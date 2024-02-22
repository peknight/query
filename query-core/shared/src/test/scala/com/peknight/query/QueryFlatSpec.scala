package com.peknight.query

import com.peknight.codec.configuration.given
import com.peknight.codec.cursor.Cursor
import com.peknight.query.Fruit.{Apple, Peach, Pear}
import com.peknight.query.codec.id.{Decoder, Encoder}
import com.peknight.query.configuration.Configuration
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
    println(Decoder[List[Fruit]].decodeS(Encoder[List[Fruit]].encode(List(Apple, Pear))))
    println(Decoder[Employee].decodeS(Encoder[Employee].encode(company.departments.head.employees.head)))
    // println(Decoder[Department].decodeS(Encoder[Department].encode(company.departments.head)))
    // println(Decoder[Company].decodeS(Encoder[Department].encode(company)))
    println(Encoder[Company].encode(company).pairs(Configuration()))
    // assert(Decoder[Company].decodeS(Encoder[Company].encode(company)).exists(_ == company))
  }
end QueryFlatSpec
