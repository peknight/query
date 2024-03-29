package com.peknight.query.http4s.ops

import cats.syntax.functor.*
import cats.{Functor, Monad}
import com.peknight.error.Error
import com.peknight.query.codec.{Decoder, Encoder}
import com.peknight.query.configuration.Configuration
import com.peknight.query.parser.parse
import org.http4s.UrlForm
object UriFormOps:
  def toUrlForm[F[_], A](a: A)(using Functor[F], Encoder[F, A], Configuration): F[UrlForm] =
    Encoder[F, A].encode(a).map(query => UrlForm(query.toMap))

  def fromUrlForm[F[_], A](urlForm: UrlForm)(using Monad[F], Decoder[F, A]): F[Either[Error, A]] =
    parse[F, A](urlForm.values)

  def urlFormWith[F[_], A](urlForm: UrlForm, a: A)(using Functor[F], Encoder[F, A], Configuration): F[UrlForm] =
    Encoder[F, A].encode(a).map(q => UrlForm(urlForm.values ++ q.toMap))
end UriFormOps
