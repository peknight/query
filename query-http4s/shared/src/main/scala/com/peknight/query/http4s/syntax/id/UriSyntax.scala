package com.peknight.query.http4s.syntax.id

import cats.Id
import com.peknight.error.Error
import com.peknight.error.parse.ParsingFailure
import com.peknight.query.codec.id.{Decoder, Encoder}
import com.peknight.query.configuration.Configuration
import com.peknight.query.http4s.ops.UriOps
import org.http4s.Uri

trait UriSyntax:
  extension (uri: Uri)
    def parseQuery[A](using Decoder[A]): Either[Error, A] = UriOps.parseQuery[Id, A](uri)
    def withQueryParams[A](a: A)(using Encoder[A], Configuration): Uri = UriOps.withQueryParams[Id, A](uri, a)
    def parseFragment[A](using Decoder[A]): Either[Error, Option[A]] =
      UriOps.parseFragment[Id, A](uri)
    def withFragmentParams[A](a: A)(using Encoder[A], Configuration): Either[ParsingFailure, Uri] =
      UriOps.withFragmentParams[Id, A](uri, a)
  end extension
end UriSyntax
object UriSyntax extends UriSyntax
