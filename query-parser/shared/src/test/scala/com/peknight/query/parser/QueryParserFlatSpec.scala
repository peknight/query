package com.peknight.query.parser

import cats.Foldable
import cats.data.{Chain, NonEmptyList}
import com.peknight.codec.configuration.given
import com.peknight.query.codec.id.Encoder
import org.scalatest.flatspec.AnyFlatSpec

class QueryParserFlatSpec extends AnyFlatSpec:
  "Query Parser" should "succeed" in {
    val iceCream = IceCream("r&u=a+", 7, true)
    val query = Encoder[IceCream].encode(iceCream).mkString()
    println(query)
    println(parse(query))
    println(parse("a[b].c="))
  }
end QueryParserFlatSpec
