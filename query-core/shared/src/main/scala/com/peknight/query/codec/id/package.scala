package com.peknight.query.codec

import cats.Id
import com.peknight.codec.cursor.Cursor
import com.peknight.query.Query

package object id:
  type Codec[A] = com.peknight.codec.Codec[Id, Query, Cursor[Query], A]
  type Encoder[A] = com.peknight.codec.Encoder[Id, Query, A]
  type Decoder[A] = com.peknight.codec.Decoder[Id, Cursor[Query], A]
end id
