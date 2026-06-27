package com.peknight.query.dsl.map.seq

import cats.Id
import com.peknight.codec.Decoder
import com.peknight.codec.cursor.Cursor
import com.peknight.error.Error
import com.peknight.query.syntax.id.query.parseWithSeq

trait Query[A](using Decoder[Id, Cursor[com.peknight.query.Query], A]):
  def unapply(params: Map[String, collection.Seq[String]]): Some[Either[Error, A]] = Query.unapply[A](params)
end Query
object Query:
  def unapply[A](params: Map[String, collection.Seq[String]])(using Decoder[Id, Cursor[com.peknight.query.Query], A])
  : Some[Either[Error, A]] =
    Some(params.parseWithSeq[A])
end Query