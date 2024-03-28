package com.peknight.query

import cats.Monad
import cats.data.{Chain, EitherT, Validated}
import cats.parse.Numbers.nonNegativeIntString
import cats.parse.{Parser, Parser0}
import cats.syntax.applicative.*
import cats.syntax.either.*
import cats.syntax.option.*
import cats.syntax.traverse.*
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.{PathElem, PathToRoot}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.codec.Decoder

import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import scala.collection.immutable.ListMap

package object parser:
  def parseKeys(map: Map[String, Chain[String]]): Validated[ParsingFailure, Map[PathToRoot, String]] =
    map.toList
      .traverse {
        case (key, values) => keyParser.parseAll(key).left.map(ParsingFailure.apply).toValidated.map {
          case (init, arrayFlagOption) => (init, arrayFlagOption, values)
        }
      }
      .map {
        _.foldLeft[Map[PathToRoot, String]](ListMap.empty[PathToRoot, String]) {
          case (acc, (init, arrayFlagOption, values)) =>
            values.uncons match
              case None => acc
              case Some((head, tail)) =>
                if arrayFlagOption.isDefined || tail.nonEmpty then
                  values.zipWithIndex.foldLeft(acc) {
                    case (map, (value, index)) => map + ((init :+ ArrayIndex(index)) -> value)
                  }
                else acc + (init -> head)
        }
      }

  def parseQuery(map: Map[PathToRoot, String]): Either[ParsingFailure, Query] =
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
        Query.Null.asRight
      else if arrayMap.isEmpty && rootOption.isEmpty then
        objectMap.toList
          .traverse { case (keyName, subMap) =>
            parseQuery(subMap).left.map(_.prependLabel(keyName)).toValidated.map(query => (keyName, query))
          }
          .map(Query.fromFields)
          .toEither
      else if objectMap.isEmpty && rootOption.isEmpty then
        val maxIndex = arrayMap.keys.max
        val acc = for i <- 0 to maxIndex yield (i, arrayMap.get(i))
        acc.toVector
          .traverse { case (index, subMapOption) =>
            subMapOption.fold(Query.Null.asRight)(subMap => parseQuery(subMap).left.map(_.prependLabel(s"$index")))
              .toValidated
          }
          .map(Query.fromValues).toEither
      else if objectMap.isEmpty && arrayMap.isEmpty then
        rootOption.fold(Query.Null)(Query.fromString).asRight
      else
        RootTypeNotMatch.value(map).asLeft
    else Query.Null.asRight

  def parse[F[_], A](input: String)(using monad: Monad[F], decoder: Decoder[F, A]): F[Either[Error, A]] =
    EitherT[F, Error, Query](pairsParser.parseAll(input)
      .left.map(ParsingFailure.apply)
      .flatMap(map => parseKeys(map).toEither)
      .flatMap(map => parseQuery(map))
      .pure[F]
    ).flatMap(query => EitherT[F, Error, A](decoder.decodeS(query).asInstanceOf[F[Either[Error, A]]])).value

  private[this] val stringParser: Parser[String] =
    Parser.charsWhile(ch => !"&=".contains(ch)).map(URLDecoder.decode(_, UTF_8))

  private[this] val pairParser: Parser[(String, String)] = (stringParser ~ (Parser.char('=') *> stringParser.?).?).map {
    case (value, None) => ("", value)
    case (key, Some(valueOption)) => (key, valueOption.getOrElse(""))
  }

  private[this] val pairsParser: Parser[Map[String, Chain[String]]] = pairParser.repSep(Parser.char('&'))
    .map { _.foldLeft[Map[String, Chain[String]]](ListMap.empty[String, Chain[String]]) { case (map, (key, value)) =>
      map + (key -> map.get(key).fold(Chain.one(value))(_.append(value)))
    }}

  private[this] val arrayIndexParser: Parser[ArrayIndex] = nonNegativeIntString.map(str => ArrayIndex(str.toLong))

  private[this] val objectKeyParser: Parser[ObjectKey] =
    (Parser.charWhere(ch => !ch.isDigit && !".[]".contains(ch)) ~ Parser.charsWhile0(ch => !".[]".contains(ch)))
      .string.map(ObjectKey.apply)

  private[this] val pathElemParser: Parser[PathElem] = arrayIndexParser | objectKeyParser

  private[this] val middlePathElemParser: Parser[PathElem] =
    (Parser.char('.') *> pathElemParser) | pathElemParser.between(Parser.char('['), Parser.char(']')).backtrack

  private[this] object ArrayFlag

  private[this] val arrayFlagParser: Parser[ArrayFlag.type] = Parser.string("[]").as(ArrayFlag)

  private[this] val keyParser: Parser0[(PathToRoot, Option[ArrayFlag.type])] =
    ((pathElemParser ~ middlePathElemParser.rep0).? ~ arrayFlagParser.?).map {
      case (None, arrayFlagOption) => (PathToRoot.empty, arrayFlagOption)
      case (Some(head, tail), arrayFlagOption) => (PathToRoot((head :: tail).toVector), arrayFlagOption)
    }

  object RootTypeNotMatch extends ParsingFailure:
    override def lowPriorityLabelMessage(label: String): Option[String] =
      s"$label: root type not match".some
  end RootTypeNotMatch

end parser
