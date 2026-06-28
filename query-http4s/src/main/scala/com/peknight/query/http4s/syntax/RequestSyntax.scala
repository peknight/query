package com.peknight.query.http4s.syntax

import cats.Monad
import cats.effect.Concurrent
import com.peknight.codec.Decoder
import com.peknight.codec.cursor.Cursor
import com.peknight.error.Error
import com.peknight.query.Query
import com.peknight.query.http4s.ops.RequestOps
import org.http4s.Request

trait RequestSyntax:
  extension [F[_]] (request: Request[F])
    def parseQuery[A](using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] =
      RequestOps.parseQuery[F, A](request)

    def parseUrlForm[G[_], A](using Concurrent[F], Monad[G], Decoder[G, Cursor[Query], A]): F[G[Either[Error, A]]] =
      RequestOps.parseUrlForm[F, G, A](request)
  end extension
end RequestSyntax
object RequestSyntax extends RequestSyntax
