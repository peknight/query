package com.peknight.query.http4s.syntax.id

import cats.Id
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.{Decoder, Encoder}
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.Query
import com.peknight.query.config.QueryConfig
import com.peknight.query.http4s.ops.UriOps
import org.http4s.Uri

trait UriSyntax:
  extension (uri: Uri)
    def parseQuery[A](using Decoder[Id, Cursor[Query], A]): Either[Error, A] = UriOps.parseQuery[Id, A](uri)
    def withQuery[A](a: A)(using Encoder[Id, Query, A], QueryConfig): Uri = UriOps.withQueryParams[Id, A](uri, a)
    def parseFragment[A](using Decoder[Id, Cursor[Query], A]): Either[Error, Option[A]] =
      UriOps.parseFragment[Id, A](uri)
    def withFragmentParams[A](a: A)(using Encoder[Id, Query, A], QueryConfig): Either[ParsingFailure, Uri] =
      UriOps.withFragmentParams[Id, A](uri, a)
  end extension
end UriSyntax
object UriSyntax extends UriSyntax
