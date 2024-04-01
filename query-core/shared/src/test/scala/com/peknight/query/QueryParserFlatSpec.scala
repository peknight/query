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
    import cats.Id
    import com.peknight.codec.Decoder.{decodeList, derived}
    import com.peknight.codec.cursor.id.Decoder
    given fruitDecoder: Decoder[Query, Fruit] = derived[Id, Query, Fruit]
    given fruitsDecoder: Decoder[Query, List[Fruit]] = decodeList[Id, Query, Fruit]
    given employeeDecoder: Decoder[Query, Employee] = derived[Id, Query, Employee]
    given employeesDecoder: Decoder[Query, List[Employee]] = decodeList[Id, Query, Employee]
    given departmentDecoder: Decoder[Query, Department] = derived[Id, Query, Department]
    given departmentsDecoder: Decoder[Query, List[Department]] = decodeList[Id, Query, Department]
    given companyDecoder: Decoder[Query, Company] = derived[Id, Query, Company]
    val company = Company("Pek", List(
      Department("X", List(Employee("A", 20, List(Fruit.Apple, Fruit.Pear)), Employee("B", 25, List(Fruit.Peach)))),
      Department("Y", List(Employee("C", 30, List(Fruit.Apple, Fruit.Peach)), Employee("D", 35, List(Fruit.Pear))))
    ))
    assert(parse[Company](company.toQueryString).exists(_ == company))
  }
end QueryParserFlatSpec
