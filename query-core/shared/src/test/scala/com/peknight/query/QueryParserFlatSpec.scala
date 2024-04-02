package com.peknight.query

import com.peknight.codec.configuration.given
import com.peknight.query.configuration.given
import com.peknight.query.parser.id.parse
import com.peknight.query.syntax.id.query.toQueryString
import org.scalatest.flatspec.AnyFlatSpec

class QueryParserFlatSpec extends AnyFlatSpec:

  "Query Parser" should "succeed with ice cream" in {
    val iceCream = IceCream("r&u=a+", 7, true)
    val tuple: (String, Int, Boolean) = Tuple.fromProductTyped(iceCream)
    assert(parse[IceCream](iceCream.toQueryString).exists(_ == iceCream))
    assert(parse[(String, Int, Boolean)](tuple.toQueryString).exists(_ == tuple))
  }

  "Query Parser" should "succeed with company" in {
    val company = Company("Pek", List(
      Department("X", List(Employee("A", 20, List(Fruit.Apple, Fruit.Pear)), Employee("B", 25, List(Fruit.Peach)))),
      Department("Y", List(Employee("C", 30, List(Fruit.Apple, Fruit.Peach)), Employee("D", 35, List(Fruit.Pear))))
    ))
    assert(parse[Company](company.toQueryString).exists(_ == company))
  }
end QueryParserFlatSpec
