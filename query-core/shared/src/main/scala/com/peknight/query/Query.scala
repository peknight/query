package com.peknight.query

import cats.data.Chain
import cats.syntax.applicative.*
import cats.syntax.either.*
import cats.syntax.option.*
import cats.syntax.traverse.*
import cats.{Applicative, Foldable, Monoid}
import com.peknight.codec.obj.Object
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.PathToRoot
import com.peknight.codec.sum.{ArrayType, NullType, ObjectType, StringType}
import com.peknight.error.parse.ParsingFailure
import com.peknight.generic.migration.Isomorphism
import com.peknight.query.configuration.ArrayOp.{Brackets, Empty, Index}
import com.peknight.query.configuration.{Configuration, PathOp}
import com.peknight.query.error.RootTypeNotMatch

import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import scala.collection.immutable.ListMap

sealed trait Query derives CanEqual:
  def fold[X](queryNull: => X, queryValue: String => X, queryArray: Vector[Query] => X, queryObject: Object[Query] => X)
  : X =
    this match
      case Query.QueryNull => queryNull
      case Query.QueryValue(value) => queryValue(value)
      case Query.QueryArray(value) => queryArray(value)
      case Query.QueryObject(value) => queryObject(value)
  def isNull: Boolean =
    this match
      case Query.QueryNull => true
      case _ => false
  def isValue: Boolean =
    this match
      case Query.QueryValue(_) => true
      case _ => false
  def isArray: Boolean =
    this match
      case Query.QueryArray(_) => true
      case _ => false
  def isObject: Boolean =
    this match
      case Query.QueryObject(_) => true
      case _ => false
  def asNull: Option[Unit] =
    this match
      case Query.QueryNull => Some(())
      case _ => None
  def asValue: Option[String] =
    this match
      case Query.QueryValue(value) => Some(value)
      case _ => None
  def asArray: Option[Vector[Query]] =
    this match
      case Query.QueryArray(value) => Some(value)
      case _ => None
  def asObject: Option[Object[Query]] =
    this match
      case Query.QueryObject(value) => Some(value)
      case _ => None
  def withNull(f: => Query): Query =
    this match
      case Query.QueryNull => f
      case _ => this
  def withValue(f: String => Query): Query =
    this match
      case Query.QueryValue(value) => f(value)
      case _ => this
  def withArray(f: Vector[Query] => Query): Query =
    this match
      case Query.QueryArray(value) => f(value)
      case _ => this
  def withObject(f: Object[Query] => Query): Query =
    this match
      case Query.QueryObject(value) => f(value)
      case _ => this
  def mapValue(f: String => String): Query =
    this match
      case Query.QueryValue(value) => Query.QueryValue(f(value))
      case _ => this
  def mapArray(f: Vector[Query] => Vector[Query]): Query =
    this match
      case Query.QueryArray(value) => Query.QueryArray(f(value))
      case _ => this
  def mapObject(f: Object[Query] => Object[Query]): Query =
    this match
      case Query.QueryObject(value) => Query.QueryObject(f(value))
      case _ => this
  def flatten: Chain[(PathToRoot, Option[String])] =
    this match
      case Query.QueryNull => Chain.one(PathToRoot.empty, None)
      case Query.QueryValue(value) => Chain.one(PathToRoot.empty, Some(value))
      case Query.QueryArray(value) =>
        Foldable[Vector].fold[Chain[(PathToRoot, Option[String])]](value.zipWithIndex.map {
          case (query, index) => query.flatten.map {
            case (pathToRoot, value) => (ArrayIndex(index) +: pathToRoot, value)
          }
        })
      case Query.QueryObject(value) =>
        Foldable[Vector].fold[Chain[(PathToRoot, Option[String])]](value.toVector.map {
          case (key, query) => query.flatten.map {
            case (pathToRoot, value) => (ObjectKey(key) +: pathToRoot, value)
          }
        })
  def pairs(using configuration: Configuration): Chain[(String, Option[String])] = Query.pairs(flatten)

  def toMap(using configuration: Configuration): Map[String, Chain[String]] = Query.toMap(pairs)

  def mkString(using Configuration): String = Query.mkString(pairs)
end Query
object Query:
  case object QueryNull extends Query:

    given [F[_]: Applicative]: Isomorphism[F, QueryNull.type, Unit] with
      def to(a: QueryNull.type): F[Unit] = ().pure
      def from(b: Unit): F[QueryNull.type] = QueryNull.pure
  end QueryNull
  case class QueryValue(value: String) extends Query
  object QueryValue:
    given [F[_]: Applicative]: Isomorphism[F, QueryValue, String] with
      def to(a: QueryValue): F[String] = a.value.pure
      def from(b: String): F[QueryValue] = QueryValue(b).pure
    end given
  end QueryValue
  case class QueryArray(value: Vector[Query]) extends Query
  object QueryArray:
    given [F[_]: Applicative]: Isomorphism[F, QueryArray, Vector[Query]] with
      def to(a: QueryArray): F[Vector[Query]] = a.value.pure
      def from(b: Vector[Query]): F[QueryArray] = QueryArray(b).pure
  end QueryArray
  case class QueryObject(value: Object[Query]) extends Query
  object QueryObject:
    given [F[_]: Applicative]: Isomorphism[F, QueryObject, Object[Query]] with
      def to(a: QueryObject): F[Object[Query]] = a.value.pure
      def from(b: Object[Query]): F[QueryObject] = QueryObject(b).pure
  end QueryObject

  val Null: Query = QueryNull
  def obj(fields: (String, Query)*): Query = fromFields(fields)
  def arr(values: Query*): Query = fromValues(values)
  def fromFields(fields: Iterable[(String, Query)]): Query = QueryObject(Object.fromIterable(fields))
  def fromValues(values: Iterable[Query]): Query = QueryArray(values.toVector)
  def fromValues(values: Query*): Query = QueryArray(values.toVector)
  def fromObject(value: Object[Query]): Query = QueryObject(value)
  def fromString(value: String): Query = QueryValue(value)

  given Monoid[Query] with
    def empty: Query = Null
    def combine(x: Query, y: Query): Query =
      (x, y) match
        case (QueryNull, b) => b
        case (a, QueryNull) => a
        case (a@QueryValue(_), b@QueryValue(_)) => fromValues(a, b)
        case (a@QueryValue(_), QueryArray(bs)) => fromValues(a +: bs)
        case (QueryValue(a), QueryObject(bs)) => fromObject(bs.add(a, Null))
        case (QueryArray(as), b@QueryValue(_)) => fromValues(as :+ b)
        case (QueryArray(as), QueryArray(bs)) => fromValues(as ++ bs)
        case (QueryArray(as), QueryObject(bs)) =>
          fromObject(Object.fromIterable(as.zipWithIndex.map(tuple => (s"${tuple._2}", tuple._1))).deepMerge(bs))
        case (QueryObject(as), QueryValue(b)) => fromObject(as.add(b, Null))
        case (QueryObject(as), QueryArray(bs)) =>
          fromObject(as.deepMerge(Object.fromIterable(bs.zipWithIndex.map(tuple => (s"${tuple._2}", tuple._1)))))
        case (QueryObject(as), QueryObject(bs)) => fromObject(as.deepMerge(bs))
  end given

  given ArrayType[Query] = ArrayType[Query](Query.fromValues, _.asArray)
  given NullType[Query] = NullType[Query](Query.Null, _.asNull)
  given StringType[Query] = StringType[Query](Query.fromString, _.asValue)
  given ObjectType[Query] = ObjectType[Query](Query.fromObject, _.asObject)

  def pairs(chain: Chain[(PathToRoot, Option[String])])(using configuration: Configuration)
  : Chain[(String, Option[String])] =
    chain.map {
      case (path, value) =>
        val elems = path.value
        if elems.isEmpty then ("", value)
        else if elems.length == 1 then
          elems.head match
            case ObjectKey(keyName) if configuration.defaultKey.contains(keyName) => ("", value)
            case ObjectKey(keyName) => (keyName, value)
            case ArrayIndex(index) =>
              configuration.lastArrayOp match
                case Index => (s"$index", value)
                case Brackets => ("[]", value)
                case Empty => ("", value)
        else
          val head = elems.head match
            case ObjectKey(keyName) => keyName
            case ArrayIndex(index) => s"$index"
          val last = elems.last match
            case ObjectKey(keyName) if configuration.defaultKey.contains(keyName) => ""
            case ObjectKey(keyName) =>
              configuration.pathOp match
                case PathOp.PathString => s".$keyName"
                case PathOp.Brackets => s"[$keyName]"
            case ArrayIndex(index) =>
              configuration.lastArrayOp match
                case Index => s"[$index]"
                case Brackets => "[]"
                case Empty => ""
          if elems.length == 2 then
            (s"$head$last", value)
          else
            val m = elems.tail.init
            val key = m.foldLeft(new StringBuilder(m.size * 5).append(head)) {
                case (sb, ObjectKey(keyName)) =>
                  configuration.pathOp match
                    case PathOp.PathString => sb.append(".").append(keyName)
                    case PathOp.Brackets => sb.append("[").append(keyName).append("]")
                case (sb, ArrayIndex(index)) => sb.append("[").append(index.toString).append("]")
              }
              .append(last).toString
            (key, value)
    }

  def toMap(chain: Chain[(String, Option[String])]): Map[String, Chain[String]] =
    chain.foldLeft(ListMap.empty[String, Chain[String]]) { case (acc, (key, valueOption)) =>
      val nextValues = acc.get(key).map(values => valueOption.fold(values)(value => values :+ value))
        .getOrElse(valueOption.fold(Chain.empty[String])(Chain.one))
      acc + (key -> nextValues)
    }

  def mkString(chain: Chain[(String, Option[String])]): String =
    chain.collect {
      case (key, Some(value)) =>
        val keyStr = URLEncoder.encode(key, UTF_8)
        val valueStr = URLEncoder.encode(value, UTF_8)
        if keyStr.isEmpty then valueStr else s"$keyStr=$valueStr"
    }.filter(_.nonEmpty).toList.mkString("&")

  def parseMap(map: Map[PathToRoot, String]): Either[ParsingFailure, Query] =
    if map.nonEmpty then
      val (objectMap, arrayMap, rootOption) = map.foldRight((ListMap.empty[String, ListMap[PathToRoot, String]],
        ListMap.empty[Int, ListMap[PathToRoot, String]], none[String])) {
        case ((pathToRoot, value), (objMap, arrMap, rootOpt)) =>
          pathToRoot.value.headOption match
            case Some(ObjectKey(keyName)) =>
              val acc = objMap.getOrElse(keyName, ListMap.empty[PathToRoot, String]) +
                (PathToRoot(pathToRoot.value.tail) -> value)
              (objMap + (keyName -> acc), arrMap, rootOpt)
            case Some(ArrayIndex(index)) =>
              val acc = arrMap.getOrElse(index.toInt, ListMap.empty[PathToRoot, String]) +
                (PathToRoot(pathToRoot.value.tail) -> value)
              (objMap, arrMap + (index.toInt -> acc), rootOpt)
            case None => (objMap, arrMap, value.some)
      }
      if objectMap.isEmpty && arrayMap.isEmpty && rootOption.isEmpty then
        Null.asRight
      else if arrayMap.isEmpty && rootOption.isEmpty then
        objectMap.toList
          .traverse { case (keyName, subMap) =>
            parseMap(subMap).left.map(_.prependLabel(keyName)).toValidated.map(query => (keyName, query))
          }
          .map(fromFields)
          .toEither
      else if objectMap.isEmpty && rootOption.isEmpty then
        val maxIndex = arrayMap.keys.max
        val acc = for i <- 0 to maxIndex yield (i, arrayMap.get(i))
        acc.toVector
          .traverse { case (index, subMapOption) =>
            subMapOption.fold(Null.asRight)(subMap => parseMap(subMap).left.map(_.prependLabel(s"$index")))
              .toValidated
          }
          .map(fromValues).toEither
      else if objectMap.isEmpty && arrayMap.isEmpty then
        rootOption.fold(Null)(fromString).asRight
      else
        RootTypeNotMatch.value(map).asLeft
    else Null.asRight
end Query
