package com.peknight.query

import com.peknight.codec.configuration.given
import com.peknight.query.Fruit.{Apple, Peach, Pear}
import com.peknight.query.codec.id.{Decoder, Encoder}
import org.scalatest.flatspec.AnyFlatSpec

class QueryFlatSpec extends AnyFlatSpec:
  "Query Codec" should "succeed with ice cream" in {
    val iceCream = IceCream("rua", 7, true)
    val tuple = Tuple.fromProductTyped(iceCream)
    assert(Decoder[IceCream].decodeS(Encoder[IceCream].encode(iceCream)).exists(_ == iceCream))
    assert(Decoder[(String, Int, Boolean)].decodeS(Encoder[(String, Int, Boolean)].encode(tuple)).exists(_ == tuple))
  }

  "Query Codec" should "succeed with company" in {
    val company = Company("Pek", List(
      Department("X", List(Employee("A", 20, List(Apple, Pear)), Employee("B", 25, List(Pear, Peach)))),
      Department("Y", List(Employee("C", 30, List(Apple, Peach)), Employee("D", 35, List(Apple))))
    ))
    assert(Decoder[Company].decodeS(Encoder[Company].encode(company)).exists(_ == company))
  }
end QueryFlatSpec
