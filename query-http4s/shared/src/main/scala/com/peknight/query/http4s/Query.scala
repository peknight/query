package com.peknight.query.http4s

import com.peknight.error.Error
import com.peknight.query.codec.id.Decoder
import com.peknight.query.syntax.id.query.parseWithSeq

object Query:
  def unapply[A](params: Map[String, collection.Seq[String]])(using Decoder[A]): Some[Either[Error, A]] =
    Some(params.parseWithSeq[A])
end Query
