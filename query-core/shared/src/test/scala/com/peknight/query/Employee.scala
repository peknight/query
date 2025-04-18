package com.peknight.query

import cats.Monad
import com.peknight.codec.Codec
import com.peknight.codec.config.given
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.sum.*

case class Employee(name: String, age: Int, favorites: List[Fruit])
object Employee:
  given codecEmployee[F[_]: Monad, S: {ObjectType, NullType, ArrayType, NumberType, StringType}]
  : Codec[F, S, Cursor[S], Employee] =
    Codec.derived[F, S, Employee]
end Employee
