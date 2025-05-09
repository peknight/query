package com.peknight.query.http4s.ops

import cats.syntax.functor.*
import cats.{Functor, Monad}
import com.peknight.error.Error
import com.peknight.query.codec.{Decoder, Encoder}
import com.peknight.query.config.Config
import com.peknight.query.parser.parseWithSeq
import com.peknight.query.syntax.query.pairs
import org.http4s.Query

object QueryOps:
  def toQuery[F[_], A](a: A)(using Functor[F], Encoder[F, A], Config): F[Query] =
    a.pairs[F].map(p => Query.fromVector(p.toVector))

  def parseQuery[F[_], A](query: Query)(using Monad[F], Decoder[F, A]): F[Either[Error, A]] =
    parseWithSeq[F, A](query.multiParams)

  def withQueryParams[F[_], A](query: Query, a: A)(using Functor[F], Encoder[F, A], Config): F[Query] =
    a.pairs[F].map(p => query ++ p.toVector)
end QueryOps
