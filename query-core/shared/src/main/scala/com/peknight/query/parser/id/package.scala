package com.peknight.query.parser

import cats.Id
import cats.data.Chain
import com.peknight.codec.Decoder
import com.peknight.codec.cursor.Cursor
import com.peknight.error.Error
import com.peknight.query.Query

package object id:
  def parse[A](input: String)(using Decoder[Id, Cursor[Query], A]): Either[Error, A] =
    com.peknight.query.parser.parse[Id, A](input)

  def parse[A](options: List[String])(shortOptionMapper: Char => String)(using Decoder[Id, Cursor[Query], A]): Either[Error, A] =
    com.peknight.query.parser.parse[Id, A](options)(shortOptionMapper)

  def parseWithChain[A](params: Map[String, Chain[String]])(using Decoder[Id, Cursor[Query], A]): Either[Error, A] =
    com.peknight.query.parser.parseWithChain[Id, A](params)

  def parseWithSeq[A](params: Map[String, collection.Seq[String]])(using Decoder[Id, Cursor[Query], A]): Either[Error, A] =
    com.peknight.query.parser.parseWithSeq[Id, A](params)
end id
