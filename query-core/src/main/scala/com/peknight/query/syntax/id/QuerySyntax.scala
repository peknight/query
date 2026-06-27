package com.peknight.query.syntax.id

import cats.Id
import cats.data.Chain
import cats.syntax.either.*
import cats.syntax.semigroup.*
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.path.PathToRoot
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.config.Config
import com.peknight.query.option.OptionKey
import com.peknight.query.parser.id.parseWithChain
import com.peknight.query.parser.{parseToQuery, parseToQueryWithChain, parseToQueryWithSeq}
import com.peknight.query.{Query, parser}

trait QuerySyntax:
  extension [A] (a: A)
    def flatten(using Encoder[Id, Query, A]): Chain[(PathToRoot, Option[String])] =
      Encoder[Id, Query, A].encode(a).flatten

    def pairs[K](using Encoder[Id, Query, A], Config[K]): Chain[(K, Option[String])] =
      Encoder[Id, Query, A].encode(a).pairs[K]

    def toMap[K](using Encoder[Id, Query, A], Config[K]): Map[K, Chain[String]] =
      Encoder[Id, Query, A].encode(a).toMap[K]

    def toQueryString(using Encoder[Id, Query, A], Config[String]): String =
      Encoder[Id, Query, A].encode(a).mkString
      
    def toOptions(using Encoder[Id, Query, A], Config[OptionKey]): List[String] =
      Encoder[Id, Query, A].encode(a).mkOptions
  end extension

  extension (input: String)
    def parse[A](using Decoder[Id, Cursor[Query], A]): Either[Error, A] = parser.id.parse[A](input)
    def withQueryParams[A](a: A)(using Encoder[Id, Query, A], Config[String]): Either[ParsingFailure, String] =
      parseToQuery(input) match
        case Right(query) => (query |+| Encoder[Id, Query, A].encode(a)).mkString.asRight[ParsingFailure]
        case Left(failure) => failure.asLeft[String]
  end extension

  extension (map: Map[String, Chain[String]])
    def parse[A](using Decoder[Id, Cursor[Query], A]): Either[Error, A] = parseWithChain[A](map)
    def withQueryParams[A](a: A)(using Encoder[Id, Query, A], Config[String]): Either[ParsingFailure, Map[String, Chain[String]]] =
      parseToQueryWithChain(map) match
        case Right(query) => (query |+| Encoder[Id, Query, A].encode(a)).toMap.asRight[ParsingFailure]
        case Left(failure) => failure.asLeft[Map[String, Chain[String]]]
  end extension

  extension (map: Map[String, collection.Seq[String]])
    def parseWithSeq[A](using Decoder[Id, Cursor[Query], A]): Either[Error, A] = parser.id.parseWithSeq[A](map)
    def withSeqQueryParams[A](a: A)(using Encoder[Id, Query, A], Config[String]): Either[ParsingFailure, Map[String, Seq[String]]] =
      parseToQueryWithSeq(map) match
        case Right(query) => (query |+| Encoder[Id, Query, A].encode(a)).toMap.map((k, v) => (k, v.toList)).asRight[ParsingFailure]
        case Left(failure) => failure.asLeft[Map[String, Seq[String]]]
  end extension
end QuerySyntax
object QuerySyntax extends QuerySyntax
