package com.peknight.query.http4s.syntax

import cats.{Functor, Monad}
import com.peknight.error.Error
import com.peknight.query.codec.{Decoder, Encoder}
import com.peknight.query.config.Config
import com.peknight.query.http4s.ops.UrlFormOps
import org.http4s.UrlForm

trait UrlFormSyntax:
  extension [A] (a: A)
    def toUrlForm[F[_]](using Functor[F], Encoder[F, A], Config): F[UrlForm] = UrlFormOps.toUrlForm[F, A](a)
  end extension

  extension (urlForm: UrlForm)
    def parse[F[_], A](using Monad[F], Decoder[F, A]): F[Either[Error, A]] = UrlFormOps.parseUrlForm[F, A](urlForm)
    def withQuery[F[_], A](a: A)(using Functor[F], Encoder[F, A], Config): F[UrlForm] =
      UrlFormOps.withQueryParams[F, A](urlForm, a)
  end extension
end UrlFormSyntax
object UrlFormSyntax extends UrlFormSyntax
