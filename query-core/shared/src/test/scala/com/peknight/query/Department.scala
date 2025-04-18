package com.peknight.query

import cats.Monad
import com.peknight.codec.Codec
import com.peknight.codec.config.given
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.sum.*

case class Department(name: String, employees: List[Employee])
object Department:
  given codecDepartment[F[_]: Monad, S: {ObjectType, NullType, ArrayType, NumberType, StringType}]
  : Codec[F, S, Cursor[S], Department] =
    Codec.derived[F, S, Department]
end Department

