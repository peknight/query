package com.peknight.query.http4s.ops

import cats.syntax.functor.*
import cats.{Functor, Monad}
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.query.Query
import com.peknight.query.config.Config
import com.peknight.query.parser.parseWithChain
import com.peknight.query.syntax.query.toMap
import org.http4s.UrlForm

object UrlFormOps:
  def toUrlForm[F[_], A](a: A)(using Functor[F], Encoder[F, Query, A], Config): F[UrlForm] =
    a.toMap[F].map(UrlForm.apply)

  def parseUrlForm[F[_], A](urlForm: UrlForm)(using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] =
    parseWithChain[F, A](urlForm.values)

  def withQueryParams[F[_], A](urlForm: UrlForm, a: A)(using Functor[F], Encoder[F, Query, A], Config): F[UrlForm] =
    a.toMap[F].map(m => UrlForm(urlForm.values ++ m))
end UrlFormOps
