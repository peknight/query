package com.peknight.query.http4s.syntax.id

import cats.Id
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.query.config.QueryConfig
import com.peknight.query.http4s.ops.QueryOps
import org.http4s.Query

trait QuerySyntax:
  extension [A] (a: A)
    def toQuery(using Encoder[Id, com.peknight.query.Query, A], QueryConfig): Query = QueryOps.toQuery[Id, A](a)
  end extension

  extension (query: Query)
    def parse[A](using Decoder[Id, Cursor[com.peknight.query.Query], A]): Either[Error, A] =
      QueryOps.parseQuery[Id, A](query)
    def withQuery[A](a: A)(using Encoder[Id, com.peknight.query.Query, A], QueryConfig): Query =
      QueryOps.withQueryParams[Id, A](query, a)
  end extension
end QuerySyntax
object QuerySyntax extends QuerySyntax
