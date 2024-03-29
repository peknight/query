package com.peknight.query.http4s.ops

import cats.syntax.functor.*
import cats.{Functor, Monad}
import com.peknight.error.Error
import com.peknight.query.codec.{Decoder, Encoder}
import com.peknight.query.configuration.Configuration
import com.peknight.query.parser.parseWithSeq
import org.http4s.Query

object QueryOps:
  def toQuery[F[_], A](a: A)(using Functor[F], Encoder[F, A], Configuration): F[Query] =
    Encoder[F, A].encode(a).map(query => Query.fromVector(query.pairs.toVector))

  def fromQuery[F[_], A](query: Query)(using Monad[F], Decoder[F, A]): F[Either[Error, A]] =
    parseWithSeq[F, A](query.multiParams)

  def queryWith[F[_], A](query: Query, a: A)(using Functor[F], Encoder[F, A], Configuration): F[Query] =
    Encoder[F, A].encode(a).map(q => query ++ q.pairs.toVector)
end QueryOps
