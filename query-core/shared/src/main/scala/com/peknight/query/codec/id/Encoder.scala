package com.peknight.query.codec.id

object Encoder:
  def apply[A](using encoder: Encoder[A]): Encoder[A] = encoder
end Encoder
