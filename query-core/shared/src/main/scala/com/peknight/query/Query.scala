package com.peknight.query

import cats.data.Chain
import cats.{Foldable, Id}
import com.peknight.codec.Object
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.PathToRoot
import com.peknight.codec.sum.{ArrayType, NullType, ObjectType, StringType}
import com.peknight.generic.migration.id.Isomorphism
import com.peknight.query.configuration.ArrayOp.{Brackets, Empty, Index}
import com.peknight.query.configuration.{Configuration, PathOp}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8

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
  def pairs(using configuration: Configuration): Chain[(String, Option[String])] =
    flatten.map {
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
  def mkString(using Configuration): String = pairs.map {
    case (key, valueOpt) =>
      val keyStr = URLEncoder.encode(key, UTF_8)
      val valueStr = valueOpt.fold("")(URLEncoder.encode(_, UTF_8))
      if keyStr.isEmpty then valueStr else s"$keyStr=$valueStr"
  }.filter(_.nonEmpty).toList.mkString("&")
end Query
object Query:
  case object QueryNull extends Query:
    given Isomorphism[QueryNull.type, Unit] with
      def to(a: QueryNull.type): Id[Unit] = ()
      def from(b: Unit): Id[QueryNull.type] = QueryNull
  end QueryNull
  case class QueryValue(value: String) extends Query
  object QueryValue:
    given Isomorphism[QueryValue, String] with
      def to(a: QueryValue): Id[String] = a.value
      def from(b: String): Id[QueryValue] = QueryValue(b)
    end given
  end QueryValue
  case class QueryArray(value: Vector[Query]) extends Query
  object QueryArray:
    given Isomorphism[QueryArray, Vector[Query]] with
      def to(a: QueryArray): Id[Vector[Query]] = a.value
      def from(b: Vector[Query]): Id[QueryArray] = QueryArray(b)
  end QueryArray
  case class QueryObject(value: Object[Query]) extends Query
  object QueryObject:
    given Isomorphism[QueryObject, Object[Query]] with
      def to(a: QueryObject): Id[Object[Query]] = a.value
      def from(b: Object[Query]): Id[QueryObject] = QueryObject(b)
  end QueryObject

  val Null: Query = QueryNull
  def obj(fields: (String, Query)*): Query = fromFields(fields)
  def arr(values: Query*): Query = fromValues(values)
  def fromFields(fields: Iterable[(String, Query)]): Query = QueryObject(Object.fromIterable(fields))
  def fromValues(values: Iterable[Query]): Query = QueryArray(values.toVector)
  def fromObject(value: Object[Query]): Query = QueryObject(value)
  def fromString(value: String): Query = QueryValue(value)

  given ArrayType[Query] = ArrayType[Query](Query.fromValues, _.asArray)
  given NullType[Query] = NullType[Query](Query.Null, _.asNull)
  given StringType[Query] = StringType[Query](Query.fromString, _.asValue)
  given ObjectType[Query] = ObjectType[Query](Query.fromObject, _.asObject)
end Query
