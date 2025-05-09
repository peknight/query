package com.peknight.query

import cats.{Monad, Show}
import com.peknight.codec.Codec
import com.peknight.codec.config.given
import com.peknight.codec.cursor.Cursor
import com.peknight.codec.sum.*

case class Company(name: String, departments: List[Department])derives CanEqual
object Company:
  given codecCompany[F[_]: Monad, S: {ObjectType, NullType, ArrayType, NumberType, StringType, Show}]
  : Codec[F, S, Cursor[S], Company] =
    Codec.derived[F, S, Company]
end Company
