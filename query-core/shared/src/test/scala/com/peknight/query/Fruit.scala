package com.peknight.query

import cats.Applicative
import com.peknight.codec.Codec
import com.peknight.codec.configuration.given
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.derivation.EnumCodecDerivation
import com.peknight.codec.sum.StringType

enum Fruit:
  case Apple, Pear, Peach
end Fruit
object Fruit:
  given stringCodecFruit[F[_]: Applicative]: Codec[F, String, String, Fruit] =
    EnumCodecDerivation.unsafeDerivedStringCodecEnum[F, Fruit]
  given codecFruit[F[_]: Applicative, S: StringType]: Codec[F, S, Cursor[S], Fruit] =
    Codec.codecS[F, S, Fruit]
end Fruit
