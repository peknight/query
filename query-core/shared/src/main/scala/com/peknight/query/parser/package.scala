package com.peknight.query

import cats.data.{Chain, EitherT, Validated}
import cats.parse.Numbers.nonNegativeIntString
import cats.parse.{Parser, Parser0}
import cats.syntax.applicative.*
import cats.syntax.either.*
import cats.syntax.foldable.*
import cats.syntax.traverse.*
import cats.{Applicative, Foldable, Monad}
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.{PathElem, PathToRoot}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.codec.Decoder

import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import scala.collection.immutable.ListMap

package object parser:

  def parseToMap(input: String): Either[ParsingFailure, Map[String, Chain[String]]] =
    if input.isEmpty then Map.empty[String, Chain[String]].asRight[ParsingFailure]
    else pairsParser.parseAll(input).left.map(ParsingFailure.apply)

  def parseToPathMapWithChain(map: Map[String, Chain[String]]): Validated[ParsingFailure, Map[PathToRoot, String]] =
    parseToPathMap(map)(_.uncons)(_.nonEmpty)(_.zipWithIndex)

  def parseToPathMapWithSeq(map: Map[String, collection.Seq[String]]): Validated[ParsingFailure, Map[PathToRoot, String]] =
    parseToPathMap(map.map((k, v) => (k, v.toSeq)))(seq => seq.headOption.map(head => (head, seq.tail)))(_.nonEmpty)(_.zipWithIndex)

  def parseToQuery(input: String): Either[ParsingFailure, Query] =
    parseToMap(input).flatMap(map => parseToPathMapWithChain(map).toEither).flatMap(Query.parseMap)

  def parseToQueryWithChain(params: Map[String, Chain[String]]): Either[ParsingFailure, Query] =
    parseToPathMapWithChain(params).toEither.flatMap(Query.parseMap)

  def parseToQueryWithSeq(params: Map[String, collection.Seq[String]]): Either[ParsingFailure, Query] =
    parseToPathMapWithSeq(params).toEither.flatMap(Query.parseMap)

  def parse[F[_], A](input: String)(using Monad[F], Decoder[F, A]): F[Either[Error, A]] = parse(parseToQuery(input))

  def parseWithChain[F[_], A](params: Map[String, Chain[String]])(using Monad[F], Decoder[F, A]): F[Either[Error, A]] =
    parse(parseToQueryWithChain(params))

  def parseWithSeq[F[_], A](params: Map[String, collection.Seq[String]])(using Monad[F], Decoder[F, A])
  : F[Either[Error, A]] =
    parse(parseToQueryWithSeq(params))

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

  private[this] def parseToPathMap[F[_]](map: Map[String, F[String]])
                                        (uncons: F[String] => Option[(String, F[String])])
                                        (nonEmpty: F[String] => Boolean)
                                        (zipWithIndex: F[String] => F[(String, Int)])
                                        (using Foldable[F], Applicative[F])
  : Validated[ParsingFailure, Map[PathToRoot, String]] =
    map.toList
      .traverse {
        case (key, v) => keyParser.parseAll(key).left.map(ParsingFailure.apply).toValidated.map {
          case (init, arrayFlagOption) => (init, arrayFlagOption, v)
        }
      }
      .map {
        _.foldLeft[Map[PathToRoot, String]](ListMap.empty[PathToRoot, String]) {
          case (acc, (init, arrayFlagOption, values)) =>
            uncons(values) match
              case Some((head, tail)) =>
                if arrayFlagOption.isDefined || nonEmpty(tail) then
                  zipWithIndex(values).foldLeft(acc) {
                    case (map, (value, index)) => map + ((init :+ ArrayIndex(index)) -> value)
                  }
                else acc + (init -> head)
              case None => acc
        }
      }

  private[this] def parse[F[_], A](parseResult: Either[ParsingFailure, Query])
                                  (using monad: Monad[F], decoder: Decoder[F, A]): F[Either[Error, A]] =
    EitherT(parseResult.pure[F])
      .flatMap(query => EitherT(decoder.decodeS(query).asInstanceOf[F[Either[Error, A]]]))
      .value

end parser
