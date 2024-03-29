package com.peknight.query.parser

import cats.Id
import cats.data.Chain
import com.peknight.error.Error
import com.peknight.query.codec.id.Decoder

package object id:
  def parse[A: Decoder](input: String): Either[Error, A] =
    com.peknight.query.parser.parse[Id, A](input)
  def parse[A: Decoder](params: Map[String, Chain[String]]): Either[Error, A] =
    com.peknight.query.parser.parse[Id, A](params)

  def parseWithSeq[A: Decoder](params: Map[String, Seq[String]]): Either[Error, A] =
    com.peknight.query.parser.parseWithSeq[Id, A](params)
end id
