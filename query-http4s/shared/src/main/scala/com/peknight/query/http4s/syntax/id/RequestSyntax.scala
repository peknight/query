package com.peknight.query.http4s.syntax.id

import cats.Id
import cats.effect.Concurrent
import com.peknight.error.Error
import com.peknight.query.codec.id.Decoder
import com.peknight.query.http4s.ops.RequestOps
import org.http4s.Request

trait RequestSyntax:
  extension [F[_]] (request: Request[F])
    def parseQuery[A](using Decoder[A]): Either[Error, A] = RequestOps.parseQuery[Id, A](request)
    def parseUrlForm[A](using Concurrent[F], Decoder[A]): F[Either[Error, A]] =
      RequestOps.parseUrlForm[F, Id, A](request)
  end extension
end RequestSyntax
object RequestSyntax extends RequestSyntax
