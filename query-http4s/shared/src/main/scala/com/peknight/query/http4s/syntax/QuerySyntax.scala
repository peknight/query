package com.peknight.query.http4s.syntax

import cats.{Functor, Monad}
import com.peknight.error.Error
import com.peknight.query.codec.{Decoder, Encoder}
import com.peknight.query.configuration.Configuration
import com.peknight.query.http4s.ops.QueryOps
import org.http4s.Query

trait QuerySyntax:
  extension [A] (a: A)
    def toQuery[F[_]](using Functor[F], Encoder[F, A], Configuration): F[Query] = QueryOps.toQuery[F, A](a)
  end extension

  extension (query: Query)
    def parse[F[_], A](using Monad[F], Decoder[F, A]): F[Either[Error, A]] = QueryOps.parseQuery[F, A](query)
    def withQuery[F[_], A](a: A)(using Functor[F], Encoder[F, A], Configuration): F[Query] =
      QueryOps.withQueryParams[F, A](query, a)
  end extension
end QuerySyntax
object QuerySyntax extends QuerySyntax
