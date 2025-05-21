package com.peknight.query.http4s.ops

import cats.Monad
import cats.effect.Concurrent
import cats.syntax.functor.*
import com.peknight.codec.Decoder
import com.peknight.codec.cursor.Cursor
import com.peknight.error.Error
import com.peknight.query.Query
import com.peknight.query.parser.{parseWithChain, parseWithSeq}
import org.http4s.{Request, UrlForm}

object RequestOps:
  def parseQuery[F[_], A](request: Request[?])(using Monad[F], Decoder[F, Cursor[Query], A]): F[Either[Error, A]] =
    parseWithSeq[F, A](request.multiParams)

  def parseUrlForm[F[_], G[_], A](request: Request[F])(using Concurrent[F], Monad[G], Decoder[G, Cursor[Query], A])
  : F[G[Either[Error, A]]] =
    request.as[UrlForm].map(urlForm => parseWithChain[G, A](urlForm.values))
end RequestOps
