package com.peknight.query.http4s.syntax.id

import cats.Id
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.query.Query
import com.peknight.query.config.QueryConfig
import com.peknight.query.http4s.ops.UrlFormOps
import org.http4s.UrlForm

trait UrlFormSyntax:
  extension [A] (a: A)
    def toUrlForm(using Encoder[Id, Query, A], QueryConfig): UrlForm = UrlFormOps.toUrlForm[Id, A](a)
  end extension

  extension (urlForm: UrlForm)
    def parse[A](using Decoder[Id, Cursor[Query], A]): Either[Error, A] = UrlFormOps.parseUrlForm[Id, A](urlForm)
    def withQuery[A](a: A)(using Encoder[Id, Query, A], QueryConfig): UrlForm =
      UrlFormOps.withQueryParams[Id, A](urlForm, a)
  end extension
end UrlFormSyntax
object UrlFormSyntax extends UrlFormSyntax
