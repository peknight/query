package com.peknight.query

import com.peknight.codec.cursor.Cursor
import com.peknight.codec.error.DecodingFailure

package object codec:
  type Codec[F[_], A] = com.peknight.codec.Codec[F, Query, Cursor[Query], DecodingFailure, A]
  type Encoder[F[_], A] = com.peknight.codec.Encoder[F, Query, A]
  type Decoder[F[_], A] = com.peknight.codec.Decoder[F, Cursor[Query], DecodingFailure, A]
end codec