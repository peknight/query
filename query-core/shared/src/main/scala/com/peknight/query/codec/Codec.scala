package com.peknight.query.codec

import com.peknight.codec.cursor.Cursor
import com.peknight.query.Query

object Codec:
  def apply[F[_], A](using Encoder[F, A], Decoder[F, A]): Codec[F, A] =
    com.peknight.codec.Codec[F, Query, Cursor[Query], A]
end Codec
