package com.peknight.query.http4s.ops

import cats.syntax.applicative.*
import cats.syntax.either.*
import cats.syntax.functor.*
import cats.syntax.option.*
import cats.{Applicative, Functor, Monad}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.codec.{Decoder, Encoder}
import com.peknight.query.configuration.Configuration
import com.peknight.query.parser.parse
import com.peknight.query.syntax.query.{toQueryString, withQueryParams}
import org.http4s.Uri

object UriOps:
  def parseQuery[F[_], A](uri: Uri)(using Monad[F], Decoder[F, A]): F[Either[Error, A]] =
    QueryOps.parseQuery[F, A](uri.query)

  def withQueryParams[F[_], A](uri: Uri, a: A)(using Functor[F], Encoder[F, A], Configuration): F[Uri] =
    QueryOps.withQueryParams[F, A](uri.query, a).map(query => uri.copy(query = query))

  def parseFragment[F[_], A](uri: Uri)(using Monad[F], Decoder[F, A]): F[Either[Error, Option[A]]] =
    uri.fragment.fold(none[A].asRight[Error].pure[F])(fragment => parse[F, A](fragment).map(_.map(Option.apply)))
    
  def withFragmentParams[F[_], A](uri: Uri, a: A)(using Applicative[F], Encoder[F, A], Configuration)
  : F[Either[ParsingFailure, Uri]] =
    uri.fragment.fold(a.toQueryString[F].map(_.asRight[ParsingFailure]))(fragment => fragment.withQueryParams[F, A](a))
      .map(_.map(uri.withFragment))
end UriOps
