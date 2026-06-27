package com.peknight.query.http4s.ops

import cats.syntax.applicative.*
import cats.syntax.either.*
import cats.syntax.functor.*
import cats.syntax.option.*
import cats.{Applicative, Functor, Monad}
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.Query
import com.peknight.query.config.Config
import com.peknight.query.parser.parse
import com.peknight.query.syntax.query.{toQueryString, withQueryParams}
import org.http4s.Uri

object UriOps:
  def parseQuery[F[_], A](uri: Uri)(using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] =
    QueryOps.parseQuery[F, A](uri.query)

  def withQueryParams[F[_], A](uri: Uri, a: A)(using Functor[F], Encoder[F, Query, A], Config[String]): F[Uri] =
    QueryOps.withQueryParams[F, A](uri.query, a).map(query => uri.copy(query = query))

  def parseFragment[F[_], A](uri: Uri)(using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, Option[A]]] =
    uri.fragment.fold(none[A].asRight[Error].pure[F])(fragment => parse[F, A](fragment).map(_.map(Option.apply)))
    
  def withFragmentParams[F[_], A](uri: Uri, a: A)(using Applicative[F], Encoder[F, Query, A], Config[String])
  : F[Either[ParsingFailure, Uri]] =
    uri.fragment.fold(a.toQueryString[F].map(_.asRight[ParsingFailure]))(fragment => fragment.withQueryParams[F, A](a))
      .map(_.map(uri.withFragment))
end UriOps
