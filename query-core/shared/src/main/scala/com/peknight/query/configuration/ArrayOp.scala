package com.peknight.query.configuration

sealed trait ArrayOp
object ArrayOp:
  case object WithIndex extends ArrayOp
  case object StripIndex extends ArrayOp
  case class Separator(separator: String) extends ArrayOp
end ArrayOp