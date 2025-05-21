package com.peknight.query.http4s.ops

import cats.syntax.functor.*
import cats.{Functor, Monad}
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.query.config.Config
import com.peknight.query.parser.parseWithSeq
import com.peknight.query.syntax.query.pairs
import org.http4s.Query

object QueryOps:
  def toQuery[F[_], A](a: A)(using Functor[F], Encoder[F, com.peknight.query.Query, A], Config): F[Query] =
    a.pairs[F].map(p => Query.fromVector(p.toVector))

  def parseQuery[F[_], A](query: Query)(using Monad[F], Decoder[F, Cursor[com.peknight.query.Query], A])
  : F[Either[Error, A]] =
    parseWithSeq[F, A](query.multiParams)

  def withQueryParams[F[_], A](query: Query, a: A)(using Functor[F], Encoder[F, com.peknight.query.Query, A], Config)
  : F[Query] =
    a.pairs[F].map(p => query ++ p.toVector)
end QueryOps
