package com.peknight.query.codec

object Decoder:
  def apply[F[_], A](using decoder: Decoder[F, A]): Decoder[F, A] = decoder
end Decoder
