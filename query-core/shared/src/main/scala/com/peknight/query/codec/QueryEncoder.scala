package com.peknight.query.codec

import com.peknight.query.Query

trait QueryEncoder[A]:
  def apply(a: A): Query
end QueryEncoder
