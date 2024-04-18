package com.peknight.query

import com.peknight.codec.cursor.Cursor

package object codec:
  type Codec[F[_], A] = com.peknight.codec.Codec[F, Query, Cursor[Query], A]
  type Encoder[F[_], A] = com.peknight.codec.Encoder[F, Query, A]
  type Decoder[F[_], A] = com.peknight.codec.Decoder[F, Cursor[Query], A]
end codec