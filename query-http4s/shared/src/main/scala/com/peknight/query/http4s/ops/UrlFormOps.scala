package com.peknight.query.http4s.ops

import cats.syntax.functor.*
import cats.{Functor, Monad}
import com.peknight.error.Error
import com.peknight.query.codec.{Decoder, Encoder}
import com.peknight.query.config.Config
import com.peknight.query.parser.parseWithChain
import com.peknight.query.syntax.query.toMap
import org.http4s.UrlForm

object UrlFormOps:
  def toUrlForm[F[_], A](a: A)(using Functor[F], Encoder[F, A], Config): F[UrlForm] =
    a.toMap[F].map(UrlForm.apply)

  def parseUrlForm[F[_], A](urlForm: UrlForm)(using Monad[F], Decoder[F, A]): F[Either[Error, A]] =
    parseWithChain[F, A](urlForm.values)

  def withQueryParams[F[_], A](urlForm: UrlForm, a: A)(using Functor[F], Encoder[F, A], Config): F[UrlForm] =
    a.toMap[F].map(m => UrlForm(urlForm.values ++ m))
end UrlFormOps
