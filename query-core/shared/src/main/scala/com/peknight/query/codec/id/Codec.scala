package com.peknight.query.codec.id

import cats.Id
import com.peknight.codec.cursor.Cursor
import com.peknight.query.Query

object Codec:
  def apply[A](using Encoder[A], Decoder[A]): Codec[A] = com.peknight.codec.Codec[Id, Query, Cursor[Query], A]
end Codec
