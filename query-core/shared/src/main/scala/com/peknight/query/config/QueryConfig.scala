package com.peknight.query.config

trait QueryConfig:
  def lastArrayOp: ArrayOp
  def pathOp: PathOp
  def defaultKeys: List[String]
  def flagKeys: List[String]
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
