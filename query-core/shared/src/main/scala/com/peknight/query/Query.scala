package com.peknight.query

import scala.collection.immutable.ListMap

sealed trait Query
object Query:
  private[query] case object QNull extends Query
  private[query] case class QValue(value: String) extends Query
  private[query] case class QArray(value: Vector[Query]) extends Query
  private[query] case class QObject(value: ListMap[String, Query]) extends Query
end Query
