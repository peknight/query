package com.peknight.query.http4s.syntax

import cats.{Applicative, Functor, Monad}
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.Query
import com.peknight.query.config.Config
import com.peknight.query.http4s.ops.UriOps
import org.http4s.Uri

trait UriSyntax:
  extension (uri: Uri)
    def parseQuery[F[_], A](using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] = UriOps.parseQuery[F, A](uri)
    def withQuery[F[_], A](a: A)(using Functor[F], Encoder[F, Query, A], Config): F[Uri] =
      UriOps.withQueryParams[F, A](uri, a)
    def parseFragment[F[_], A](using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, Option[A]]] =
      UriOps.parseFragment[F, A](uri)
    def withFragmentParams[F[_], A](a: A)(using Applicative[F], Encoder[F, Query, A], Config)
    : F[Either[ParsingFailure, Uri]] =
      UriOps.withFragmentParams[F, A](uri, a)
  end extension
end UriSyntax
object UriSyntax extends UriSyntax
