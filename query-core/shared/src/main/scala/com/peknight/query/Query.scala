package com.peknight.query

import cats.data.Chain
import cats.data.Chain.==:
import cats.syntax.applicative.*
import cats.syntax.either.*
import cats.syntax.eq.*
import cats.syntax.option.*
import cats.syntax.show.*
import cats.syntax.traverse.*
import cats.{Applicative, Foldable, Monoid, Show}
import com.peknight.codec.Decoder
import com.peknight.codec.number.Number
import com.peknight.codec.obj.Object
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.PathToRoot
import com.peknight.codec.sum.*
import com.peknight.error.parse.ParsingFailure
import com.peknight.generic.migration.Isomorphism
import com.peknight.query.config.Config
import com.peknight.query.error.RootTypeNotMatch
import com.peknight.query.option.{ArgumentStyle, OptionKey, OptionKeyType}
import spire.math.Interval

import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import scala.collection.immutable.ListMap

sealed trait Query derives CanEqual:
  def fold[X](queryNull: => X, queryValue: String => X, queryArray: Vector[Query] => X, queryObject: Object[String, Query] => X)
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
  def asObject: Option[Object[String, Query]] =
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
  def withObject(f: Object[String, Query] => Query): Query =
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
  def mapObject(f: Object[String, Query] => Object[String, Query]): Query =
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
        if value.isEmpty then Chain.one(PathToRoot.empty, Some(""))
        else
          Foldable[Vector].fold[Chain[(PathToRoot, Option[String])]](value.toVector.map {
            case (key, query) => query.flatten.map {
              case (pathToRoot, value) => (ObjectKey(key) +: pathToRoot, value)
            }
          })

  def pairsEither[K](using config: Config[K]): Chain[(K, Either[String, Option[String]])] =
    Query.pairsEither[K](flatten)

  def pairsOption[K](using config: Config[K]): Chain[(K, Option[String])] =
    Query.pairsOption[K](flatten)

  def pairs[K](using config: Config[K]): Chain[(K, Option[String])] =
    Query.pairs[K](flatten)

  def toMapEither[K](using config: Config[K]): Map[K, Either[String, Chain[String]]] =
    Query.toMapEither[K](pairsEither)

  def toMapOption[K](using config: Config[K]): Map[K, Chain[String]] = Query.toMapOption[K](pairsEither)

  def toMap[K](using config: Config[K]): Map[K, Chain[String]] = Query.toMap[K](pairsEither)

  def mkString(using Config[String]): String = Query.mkString(pairsEither)

  def mkOptions(using Config[OptionKey]): List[String] = Query.mkOptions(toMapEither)
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
  case class QueryObject(value: Object[String, Query]) extends Query
  object QueryObject:
    given [F[_]: Applicative]: Isomorphism[F, QueryObject, Object[String, Query]] with
      def to(a: QueryObject): F[Object[String, Query]] = a.value.pure
      def from(b: Object[String, Query]): F[QueryObject] = QueryObject(b).pure
  end QueryObject

  val Null: Query = QueryNull
  def obj(fields: (String, Query)*): Query = fromFields(fields)
  def arr(values: Query*): Query = fromValues(values)
  def fromFields(fields: Iterable[(String, Query)]): Query = QueryObject(Object.fromIterable(fields))
  def fromValues(values: Iterable[Query]): Query = QueryArray(values.toVector)
  def fromValues(values: Query*): Query = QueryArray(values.toVector)
  def fromObject(value: Object[String, Query]): Query = QueryObject(value)
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
  given NumberType[Query] = NumberType[Query](number => Query.fromString(number.toString), _.asValue.flatMap(Number.fromString))
  given BooleanType[Query] = BooleanType[Query](flag => Query.fromString(flag.toString), _.asValue.flatMap(Decoder.toBooleanOption))
  given Show[Query] with
    def show(t: Query): String = t match {
      case QueryNull => "null"
      case QueryValue(value) => value
      case QueryArray(value) => value.mkString("[", ",", "]")
      case QueryObject(value) => value.show
    }
  end given

  def pairsEither[K](chain: Chain[(PathToRoot, Option[String])])(using config: Config[K])
  : Chain[(K, Either[String, Option[String]])] =
    chain.map {
      case (path, value) =>
        val key = config.toKeys(path).head
        val show =
          val elems = path.value
          if elems.isEmpty then value.asRight
          else if elems.length == 1 then
            elems.head match
              case ObjectKey(keyName) =>
                // flagKey的场景中，没有值以None返回，有值以Left返回
                // 非flagKey的场景中，以Option返回
                value.filter(_ => config.flagKeys.contains(keyName)) match
                  case Some(v) => v.asLeft
                  case None => value.asRight
              case ArrayIndex(index) => value.asRight
          else {
            // 同理
            value.filter { _ =>
              elems.last match
                case ObjectKey(keyName) => config.flagKeys.contains(keyName)
                case ArrayIndex(index) => false
            }.fold(value.asRight)(_.asLeft)
          }
        (key, show)
    }

  def pairsOption[K](chain: Chain[(PathToRoot, Option[String])])(using config: Config[K])
  : Chain[(K, Option[String])] =
    pairsEither[K](chain).map(tuple => (tuple._1, tuple._2.fold(Some(_), identity)))

  def pairs[K](chain: Chain[(PathToRoot, Option[String])])(using config: Config[K])
  : Chain[(K, Option[String])] =
    pairsEither[K](chain)
      .collect {
        case (key, Right(Some(value))) => (key, Some(value))
        case (key, Left(_)) => (key, None)
      }

  def toMapEither[K](chain: Chain[(K, Either[String, Option[String]])]): Map[K, Either[String, Chain[String]]] =
    chain.foldLeft(ListMap.empty[K, Either[String, Chain[String]]]) { case (acc, (key, valueEither)) =>
      val nextValues = acc.get(key)
        .map { valuesEither =>
          (valuesEither, valueEither) match
            case (Left(flagValueA), Left(flagValueB)) => Chain(flagValueA, flagValueB).asRight[String]
            case (Left(flagValue), Right(None)) => flagValue.asLeft[Chain[String]]
            case (Left(flagValue), Right(Some(value))) => Chain(flagValue, value).asRight[String]
            case (Right(Chain()), Left(flagValue)) => flagValue.asLeft[Chain[String]]
            case (Right(chain), Left(flagValue)) => (chain :+ flagValue).asRight[String]
            case (Right(chain), Right(None)) => chain.asRight[String]
            case (Right(chain), Right(Some(value))) => (chain :+ value).asRight[String]
        }
        .getOrElse { valueEither match
          case Left(flagValue) => flagValue.asLeft[Chain[String]]
          case Right(None) => Chain.empty[String].asRight[String]
          case Right(Some(value)) => Chain.one(value).asRight[String]
        }
      acc + (key -> nextValues)
    }

  def toMapOption[K](chain: Chain[(K, Either[String, Option[String]])]): Map[K, Chain[String]] =
    toMapEither[K](chain).map(tuple => (tuple._1, tuple._2.fold(Chain.one, identity)))

  def toMap[K](chain: Chain[(K, Either[String, Option[String]])]): Map[K, Chain[String]] =
    toMapEither[K](chain).collect {
      case (key, Right(chain @ (head ==: tail))) => (key, chain)
      case (key, Left(_)) => (key, Chain.empty[String])
    }

  def mkString(chain: Chain[(String, Either[String, Option[String]])]): String =
    chain.collect {
      case (key, Right(Some(value))) =>
        val keyStr = URLEncoder.encode(key, UTF_8)
        val valueStr = URLEncoder.encode(value, UTF_8)
        if keyStr.isEmpty then valueStr else s"$keyStr=$valueStr"
      case (key, Left(_)) => URLEncoder.encode(key, UTF_8)
    }.filter(_.nonEmpty).toList.mkString("&")

  def mkOptions(mapEither: Map[OptionKey, Either[String, Chain[String]]]): List[String] =
    mapEither.filterNot(_._2.exists(_.isEmpty))
      .groupBy((key, either) => (key.keyType, key.combinable && either.isLeft, key.argumentStyle))
      .toList.flatMap {
        case ((OptionKeyType.None, _, _), _) => List.empty[String]
        case ((keyType, false, argumentStyle), map) => map.toList.flatMap { (key, either) =>
          val k = s"${keyType.prefix.getOrElse("")}${key.key}"
          val chain = either.fold(Chain.one, identity)
          val length = chain.length.toInt
          if key.argumentLength.intersect(Interval.above(0)).isEmpty || either.isLeft then List(k)
          else if length > 1 && key.argumentLength.contains(length) then k :: chain.toList
          else chain.toList.flatMap(value =>
            if argumentStyle === ArgumentStyle.EqualsSeparated then List(s"$k=$value") else List(k, value)
          )
        }
        case ((keyType, true, _), map) => List(s"${keyType.prefix.getOrElse("")}${map.keySet.map(_.key).mkString}")
      }

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
        RootTypeNotMatch.value(map)(using Show.fromToString[Map[PathToRoot, String]]).asLeft
    else Null.asRight
end Query
