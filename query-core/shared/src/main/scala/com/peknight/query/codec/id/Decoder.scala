package com.peknight.query.codec.id

object Decoder:
  def apply[A](using decoder: Decoder[A]): Decoder[A] = decoder
end Decoder
