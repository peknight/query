package com.peknight.query.configuration

trait Configuration:
  def lastArrayOp: ArrayOp
  def pathOp: PathOp
  def defaultKey: Option[String]
end Configuration
