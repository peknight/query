package com.peknight.query.parser

import cats.Id
import com.peknight.error.Error
import com.peknight.query.codec.id.Decoder

package object id:
  def parse[A: Decoder](input: String): Either[Error, A] = com.peknight.query.parser.parse[Id, A](input)
end id
