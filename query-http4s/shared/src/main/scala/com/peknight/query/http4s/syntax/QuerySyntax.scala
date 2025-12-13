package com.peknight.query.http4s.syntax

import cats.{Functor, Monad}
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.query.config.Config
import com.peknight.query.http4s.ops.QueryOps
import org.http4s.Query

trait QuerySyntax:
  extension [A] (a: A)
    def toQuery[F[_]](using Functor[F], Encoder[F, com.peknight.query.Query, A], Config[String]): F[Query] =
      QueryOps.toQuery[F, A](a)
  end extension

  extension (query: Query)
    def parse[F[_], A](using Monad[F], Decoder[F, Cursor[com.peknight.query.Query], A]): F[Either[Error, A]] = 
      QueryOps.parseQuery[F, A](query)
    def withQuery[F[_], A](a: A)(using Functor[F], Encoder[F, com.peknight.query.Query, A], Config[String]): F[Query] =
      QueryOps.withQueryParams[F, A](query, a)
  end extension
end QuerySyntax
object QuerySyntax extends QuerySyntax
