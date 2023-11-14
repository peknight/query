package com.peknight.query

import com.peknight.codec.cursor.Cursor
import com.peknight.codec.error.DecodingFailure
import com.peknight.codec.id.{Decoder, Encoder}

package object codec:
  type QueryEncoder[A] = Encoder[Query, A]
  type QueryDecoder[A] = Decoder[Cursor[Query], DecodingFailure[Cursor[Query]], A]
end codec
