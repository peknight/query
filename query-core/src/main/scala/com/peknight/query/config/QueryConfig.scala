package com.peknight.query.config

import cats.data.NonEmptyList
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.PathToRoot
import com.peknight.query.config.ArrayOp.{Brackets, Empty, Index}

trait QueryConfig extends Config[String]:
  def toKeys(pathToRoot: PathToRoot): NonEmptyList[String] = NonEmptyList.one(toKey(pathToRoot))
end QueryConfig
object QueryConfig:
  private case class QueryConfig(
    lastArrayOp: ArrayOp,
    pathOp: PathOp,
    defaultKeys: List[String],
    flagKeys: List[String]
  ) extends com.peknight.query.config.QueryConfig

  def apply(
    lastArrayOp: ArrayOp = ArrayOp.Brackets,
    pathOp: PathOp = PathOp.PathString,
    defaultKeys: List[String] = Nil,
    flagKeys: List[String] = Nil
  ): com.peknight.query.config.QueryConfig =
    QueryConfig(lastArrayOp, pathOp, defaultKeys, flagKeys)

  val default: com.peknight.query.config.QueryConfig = apply()
end QueryConfig
