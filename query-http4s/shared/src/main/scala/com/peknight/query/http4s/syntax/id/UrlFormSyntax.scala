package com.peknight.query.http4s.syntax.id

import cats.Id
import com.peknight.error.Error
import com.peknight.query.codec.id.{Decoder, Encoder}
import com.peknight.query.config.Config
import com.peknight.query.http4s.ops.UrlFormOps
import org.http4s.UrlForm

trait UrlFormSyntax:
  extension [A] (a: A)
    def toUrlForm(using Encoder[A], Config): UrlForm = UrlFormOps.toUrlForm[Id, A](a)
  end extension

  extension (urlForm: UrlForm)
    def parse[A](using Decoder[A]): Either[Error, A] = UrlFormOps.parseUrlForm[Id, A](urlForm)
    def withQuery[A](a: A)(using Encoder[A], Config): UrlForm =
      UrlFormOps.withQueryParams[Id, A](urlForm, a)
  end extension
end UrlFormSyntax
object UrlFormSyntax extends UrlFormSyntax
