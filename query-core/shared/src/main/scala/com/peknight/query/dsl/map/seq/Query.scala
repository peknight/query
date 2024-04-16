package com.peknight.query.dsl.map.seq

import com.peknight.error.Error
import com.peknight.query.codec.id.Decoder
import com.peknight.query.syntax.id.query.parseWithSeq

trait Query[A](using Decoder[A]):
  def unapply(params: Map[String, collection.Seq[String]]): Some[Either[Error, A]] = Query.unapply[A](params)
end Query
object Query:
  def unapply[A: Decoder](params: Map[String, collection.Seq[String]]): Some[Either[Error, A]] =
    Some(params.parseWithSeq[A])
end Query