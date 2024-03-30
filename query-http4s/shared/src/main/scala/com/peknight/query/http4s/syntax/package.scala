package com.peknight.query.http4s

package object syntax:
  object all extends QuerySyntax with UrlFormSyntax with UriSyntax with RequestSyntax
  object query extends QuerySyntax
  object urlForm extends UrlFormSyntax
  object uri extends UriSyntax
  object request extends RequestSyntax
end syntax
