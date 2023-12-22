package com.peknight.query.ops

import cats.data.Chain

sealed trait KeyOp:
  def join(keys: Chain[String | Int]): String
object KeyOp:
  case object Brackets extends KeyOp:
    def join(keys: Chain[String | Int]): String =
      keys.uncons match
        case None => ""
        case Some(head, tail) => s"$head${tail.map(key => s"[$key]").toList.mkString}"
  end Brackets
end KeyOp
