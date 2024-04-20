package com.peknight.query.configuration

trait Configuration:
  def lastArrayOp: ArrayOp
  def pathOp: PathOp
  def defaultKeys: List[String]
  def flagKeys: List[String]
end Configuration
object Configuration:
  private[this] case class Configuration(
    lastArrayOp: ArrayOp,
    pathOp: PathOp,
    defaultKeys: List[String],
    flagKeys: List[String]
  ) extends com.peknight.query.configuration.Configuration

  def apply(
    lastArrayOp: ArrayOp = ArrayOp.Brackets,
    pathOp: PathOp = PathOp.PathString,
    defaultKeys: List[String] = Nil,
    flagKeys: List[String] = Nil
  ): com.peknight.query.configuration.Configuration =
    Configuration(lastArrayOp, pathOp, defaultKeys, flagKeys)

  val default: com.peknight.query.configuration.Configuration = apply()
end Configuration
