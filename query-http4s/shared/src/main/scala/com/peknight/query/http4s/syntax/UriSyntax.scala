package com.peknight.query.http4s.syntax

import cats.{Applicative, Functor, Monad}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.codec.{Decoder, Encoder}
import com.peknight.query.configuration.Configuration
import com.peknight.query.http4s.ops.UriOps
import org.http4s.Uri

trait UriSyntax:
  extension (uri: Uri)
    def parseQuery[F[_], A](using Monad[F], Decoder[F, A]): F[Either[Error, A]] = UriOps.parseQuery[F, A](uri)
    def withQueryParams[F[_], A](a: A)(using Functor[F], Encoder[F, A], Configuration): F[Uri] =
      UriOps.withQueryParams[F, A](uri, a)
    def parseFragment[F[_], A](using Monad[F], Decoder[F, A]): F[Either[Error, Option[A]]] =
      UriOps.parseFragment[F, A](uri)
    def withFragmentParams[F[_], A](a: A)(using Applicative[F], Encoder[F, A], Configuration)
    : F[Either[ParsingFailure, Uri]] =
      UriOps.withFragmentParams[F, A](uri, a)
  end extension
end UriSyntax
object UriSyntax extends UriSyntax
