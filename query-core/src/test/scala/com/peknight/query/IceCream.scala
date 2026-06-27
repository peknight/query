package com.peknight.query

import cats.{Monad, Show}
import com.peknight.codec.Codec
import com.peknight.codec.config.given
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.sum.*

case class IceCream(name: String, numCherries: Int, inCone: Boolean) derives CanEqual
object IceCream:
  given codecIceCream[F[_]: Monad, S: {ObjectType, NullType, ArrayType, BooleanType, NumberType, StringType, Show}]
  : Codec[F, S, Cursor[S], IceCream] =
    Codec.derived[F, S, IceCream]
end IceCream
