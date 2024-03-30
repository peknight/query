package com.peknight.query.http4s.syntax

package object id:
  object all extends QuerySyntax with UrlFormSyntax with UriSyntax with RequestSyntax
  object query extends QuerySyntax
  object urlForm extends UrlFormSyntax
  object uri extends UriSyntax
  object request extends RequestSyntax
end id
