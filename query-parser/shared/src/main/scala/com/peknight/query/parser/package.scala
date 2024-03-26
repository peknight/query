package com.peknight.query

import cats.data.{Chain, NonEmptyList, ValidatedNel}
import cats.parse.Numbers.nonNegativeIntString
import cats.parse.Parser.Error
import cats.parse.{Parser, Parser0}
import cats.syntax.either.*
import cats.syntax.traverse.*
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.{PathElem, PathToRoot}

import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import scala.collection.immutable.ListMap

package object parser:

  val stringParser: Parser[String] =
    Parser.charsWhile(ch => !"&=".contains(ch)).map(URLDecoder.decode(_, UTF_8))
  val pairParser: Parser[(String, String)] = (stringParser ~ (Parser.char('=') *> stringParser.?).?).map {
    case (value, None) => ("", value)
    case (key, Some(valueOption)) => (key, valueOption.getOrElse(""))
  }
  val pairsParser: Parser[Map[String, Chain[String]]] = pairParser.repSep(Parser.char('&'))
    .map { _.foldLeft[Map[String, Chain[String]]](ListMap.empty[String, Chain[String]]) { case (map, (key, value)) =>
      map + (key -> map.get(key).fold(Chain.one(value))(_.append(value)))
    }}

  private[this] object ArrayFlag
  val arrayIndexParser: Parser[ArrayIndex] = nonNegativeIntString.map(str => ArrayIndex(str.toLong))
  val objectKeyParser: Parser[ObjectKey] =
    (Parser.charWhere(ch => !ch.isDigit && !".[]".contains(ch)) ~ Parser.charsWhile0(ch => !".[]".contains(ch)))
      .string.map(ObjectKey.apply)
  val pathElemParser: Parser[PathElem] = arrayIndexParser | objectKeyParser
  val middlePathElemParser: Parser[PathElem] =
    (Parser.char('.') *> pathElemParser) | pathElemParser.between(Parser.char('['), Parser.char(']')).backtrack
  val arrayFlagParser: Parser[ArrayFlag.type] = Parser.string("[]").as(ArrayFlag)
  val keyParser: Parser0[(PathToRoot, Option[ArrayFlag.type])] =
    ((pathElemParser ~ middlePathElemParser.rep0).? ~ arrayFlagParser.?).map {
      case (None, arrayFlagOption) => (PathToRoot.empty, arrayFlagOption)
      case (Some(head, tail), arrayFlagOption) => (PathToRoot((head :: tail).toVector), arrayFlagOption)
    }

  def parseKeys(map: Map[String, Chain[String]]): ValidatedNel[Error, Map[PathToRoot, Chain[String]]] =
    map.toList
      .traverse {
        case (key, values) => keyParser.parseAll(key).toValidatedNel.map {
          case (init, arrayFlagOption) => (init, arrayFlagOption, values)
        }
      }
      .map {
        _.foldLeft[(Map[PathToRoot, Chain[String]], Map[PathToRoot, Int])](
          (ListMap.empty[PathToRoot, Chain[String]], Map.empty[PathToRoot, Int])
        ) {
          case ((acc, indexMap), (init, arrayFlagOption, values)) =>
            arrayFlagOption match
              case Some(_) =>
                val index = indexMap.get(init).map(_ + 1).getOrElse(0)
                (acc + ((init :+ ArrayIndex(index)) -> values), indexMap + (init -> index))
              case None =>
                (acc + (init -> values), indexMap)
        }._1
      }

  def parse(input: String) = pairsParser.parseAll(input).left.map(NonEmptyList.one).flatMap(nel => parseKeys(nel).toEither)

  def parseQuery(map: Map[PathToRoot, Chain[String]]) =


    ???
end parser
