package com.peknight.query.codec

object Encoder:
  def apply[F[_], A](using encoder: Encoder[F, A]): Encoder[F, A] = encoder
end Encoder
