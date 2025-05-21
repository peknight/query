package com.peknight.query.syntax

import cats.data.Chain
import cats.syntax.applicative.*
import cats.syntax.either.*
import cats.syntax.functor.*
import cats.syntax.semigroup.*
import cats.{Applicative, Functor, Monad}
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.path.PathToRoot
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.config.Config
import com.peknight.query.parser.{parseToQuery, parseToQueryWithChain, parseToQueryWithSeq}
import com.peknight.query.{Query, parser}

trait QuerySyntax:
  extension [A] (a: A)
    def flatten[F[_]](using Functor[F], Encoder[F, Query, A]): F[Chain[(PathToRoot, Option[String])]] =
      Encoder[F, Query, A].encode(a).map(_.flatten)

    def pairs[F[_]](using Functor[F], Encoder[F, Query, A], Config): F[Chain[(String, Option[String])]] =
      Encoder[F, Query, A].encode(a).map(_.pairs)

    def toMap[F[_]](using Functor[F], Encoder[F, Query, A], Config): F[Map[String, Chain[String]]] =
      Encoder[F, Query, A].encode(a).map(_.toMap)

    def toQueryString[F[_]](using Functor[F], Encoder[F, Query, A], Config): F[String] =
      Encoder[F, Query, A].encode(a).map(_.mkString)
  end extension

  extension (input: String)
    def parse[F[_], A](using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] = parser.parse[F, A](input)
    def withQueryParams[F[_], A](a: A)(using Applicative[F], Encoder[F, Query, A], Config)
    : F[Either[ParsingFailure, String]] =
      parseToQuery(input) match
        case Right(query) => Encoder[F, Query, A].encode(a).map(query |+| _).map(_.mkString.asRight[ParsingFailure])
        case Left(failure) => failure.asLeft[String].pure[F]
  end extension

  extension (map: Map[String, Chain[String]])
    def parse[F[_], A](using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] = parser.parseWithChain[F, A](map)
    def withQueryParams[F[_], A](a: A)(using Applicative[F], Encoder[F, Query, A], Config)
    : F[Either[ParsingFailure, Map[String, Chain[String]]]] =
      parseToQueryWithChain(map) match
        case Right(query) => Encoder[F, Query, A].encode(a).map(query |+| _).map(_.toMap.asRight[ParsingFailure])
        case Left(failure) => failure.asLeft[Map[String, Chain[String]]].pure[F]
  end extension
  extension (map: Map[String, collection.Seq[String]])
    def parseWithSeq[F[_], A](using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] = parser.parseWithSeq[F, A](map)
    def withSeqQueryParams[F[_], A](a: A)(using Applicative[F], Encoder[F, Query, A], Config)
    : F[Either[ParsingFailure, Map[String, Seq[String]]]] =
      parseToQueryWithSeq(map) match
        case Right(query) =>
          Encoder[F, Query, A].encode(a).map(query |+| _).map(_.toMap.map((k, v) => (k, v.toList)).asRight[ParsingFailure])
        case Left(failure) => failure.asLeft[Map[String, Seq[String]]].pure[F]
  end extension
end QuerySyntax
object QuerySyntax extends QuerySyntax
