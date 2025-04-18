package com.peknight.query.config

trait Config:
  def lastArrayOp: ArrayOp
  def pathOp: PathOp
  def defaultKeys: List[String]
  def flagKeys: List[String]
end Config
object Config:
  private case class Config(
    lastArrayOp: ArrayOp,
    pathOp: PathOp,
    defaultKeys: List[String],
    flagKeys: List[String]
  ) extends com.peknight.query.config.Config

  def apply(
    lastArrayOp: ArrayOp = ArrayOp.Brackets,
    pathOp: PathOp = PathOp.PathString,
    defaultKeys: List[String] = Nil,
    flagKeys: List[String] = Nil
  ): com.peknight.query.config.Config =
    Config(lastArrayOp, pathOp, defaultKeys, flagKeys)

  val default: com.peknight.query.config.Config = apply()
end Config
