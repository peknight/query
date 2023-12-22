package com.peknight.query.ops

import cats.data.Chain

sealed trait ArrayOp
object ArrayOp:
  sealed trait ArrayKeyOp extends ArrayOp

  case object Index extends ArrayKeyOp
  case object Brackets extends ArrayKeyOp:
    val brackets: String = "[]"
  end Brackets
  case object Empty extends ArrayKeyOp


  sealed trait ArrayValueOp extends ArrayOp:
    def join(values: Chain[String]): String
  end ArrayValueOp

  case class MkString(start: String = "", sep: String = ",", end: String = "") extends ArrayValueOp:
    def join(values: Chain[String]): String = values.toList.mkString(start, sep, end)
  end MkString
  case class Join(f: Chain[String] => String) extends ArrayValueOp:
    def join(values: Chain[String]): String = f(values)
  end Join
end ArrayOp

