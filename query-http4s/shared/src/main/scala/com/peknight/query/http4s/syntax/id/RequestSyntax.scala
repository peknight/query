package com.peknight.query.http4s.syntax.id

import cats.Id
import cats.effect.Concurrent
import com.peknight.codec.Decoder
import com.peknight.codec.cursor.Cursor
import com.peknight.error.Error
import com.peknight.query.Query
import com.peknight.query.http4s.ops.RequestOps
import org.http4s.Request

trait RequestSyntax:
  extension [F[_]] (request: Request[F])
    def parseQuery[A](using Decoder[Id, Cursor[Query], A]): Either[Error, A] = RequestOps.parseQuery[Id, A](request)
    def parseUrlForm[A](using Concurrent[F], Decoder[Id, Cursor[Query], A]): F[Either[Error, A]] =
      RequestOps.parseUrlForm[F, Id, A](request)
  end extension
end RequestSyntax
object RequestSyntax extends RequestSyntax
