package com.peknight.query.http4s.syntax

import cats.{Functor, Monad}
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.query.Query
import com.peknight.query.config.Config
import com.peknight.query.http4s.ops.UrlFormOps
import org.http4s.UrlForm

trait UrlFormSyntax:
  extension [A] (a: A)
    def toUrlForm[F[_]](using Functor[F], Encoder[F, Query, A], Config): F[UrlForm] = UrlFormOps.toUrlForm[F, A](a)
  end extension

  extension (urlForm: UrlForm)
    def parse[F[_], A](using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] =
      UrlFormOps.parseUrlForm[F, A](urlForm)
    def withQuery[F[_], A](a: A)(using Functor[F], Encoder[F, Query, A], Config): F[UrlForm] =
      UrlFormOps.withQueryParams[F, A](urlForm, a)
  end extension
end UrlFormSyntax
object UrlFormSyntax extends UrlFormSyntax
