package com.peknight.query.codec

import com.peknight.query.cursor.Cursor
import com.peknight.query.error.DecodingFailure

trait QueryDecoder[A]:
  def apply(cursor: Cursor): Either[DecodingFailure, A]
end QueryDecoder

