package com.peknight.query.configuration

trait Configuration:
  def lastArrayOp: ArrayOp
  def pathOp: PathOp
  def defaultKey: Option[String]
end Configuration
object Configuration:
  private[this] case class Configuration(
    lastArrayOp: ArrayOp,
    pathOp: PathOp,
    defaultKey: Option[String]
  ) extends com.peknight.query.configuration.Configuration

  def apply(
    lastArrayOp: ArrayOp = ArrayOp.Brackets,
    pathOp: PathOp = PathOp.PathString,
    defaultKey: Option[String] = None
  ): com.peknight.query.configuration.Configuration =
    Configuration(lastArrayOp, pathOp, defaultKey)

  val default: com.peknight.query.configuration.Configuration = apply()
end Configuration
