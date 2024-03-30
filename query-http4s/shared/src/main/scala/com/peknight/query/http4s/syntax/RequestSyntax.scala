package com.peknight.query.http4s.syntax

import com.peknight.error.Error
import cats.Monad
import cats.effect.Concurrent
import com.peknight.query.codec.Decoder
import com.peknight.query.http4s.ops.RequestOps
import org.http4s.Request

trait RequestSyntax:
  extension [F[_]] (request: Request[F])
    def parseQuery[A](using Monad[F], Decoder[F, A]): F[Either[Error, A]] =
      RequestOps.parseQuery[F, A](request)

    def parseUrlForm[G[_], A](using Concurrent[F], Monad[G], Decoder[G, A]): F[G[Either[Error, A]]] =
      RequestOps.parseUrlForm[F, G, A](request)
  end extension
end RequestSyntax
object RequestSyntax extends RequestSyntax
