package com.peknight.query.http4s.syntax.id

import cats.Id
import com.peknight.error.Error
import com.peknight.query.codec.id.{Decoder, Encoder}
import com.peknight.query.configuration.Configuration
import com.peknight.query.http4s.ops.QueryOps
import org.http4s.Query

trait QuerySyntax:
  extension [A] (a: A)
    def toQuery(using Encoder[A], Configuration): Query = QueryOps.toQuery[Id, A](a)
  end extension

  extension (query: Query)
    def parse[A](using Decoder[A]): Either[Error, A] = QueryOps.parseQuery[Id, A](query)
    def withQuery[A](a: A)(using Encoder[A], Configuration): Query = QueryOps.withQueryParams[Id, A](query, a)
  end extension
end QuerySyntax
object QuerySyntax extends QuerySyntax
