package com.peknight.query

import com.peknight.codec.configuration.given
import com.peknight.query.codec.id.Encoder
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
    // import cats.Id
    // import com.peknight.codec.cursor.Cursor
    // import com.peknight.codec.error.DecodingFailure
    // import com.peknight.query.Fruit.{Apple, Peach, Pear}
    // given fruitDecoder: com.peknight.codec.Decoder[Id, Cursor[Query], DecodingFailure, Fruit] =
    //   com.peknight.codec.Decoder.derived[Id, Query, Cursor[Query], DecodingFailure, Fruit]
    // given fruitsDecoder: com.peknight.codec.Decoder[Id, Cursor[Query], DecodingFailure, List[Fruit]] =
    //   com.peknight.codec.Decoder.decodeList[Id, Query, Fruit]
    // given employeeDecoder: com.peknight.codec.Decoder[Id, Cursor[Query], DecodingFailure, Employee] =
    //   com.peknight.codec.Decoder.derived[Id, Query, Cursor[Query], DecodingFailure, Employee]
    // given employeesDecoder: com.peknight.codec.Decoder[Id, Cursor[Query], DecodingFailure, List[Employee]] =
    //   com.peknight.codec.Decoder.decodeList[Id, Query, Employee]
    // given departmentDecoder: com.peknight.codec.Decoder[Id, Cursor[Query], DecodingFailure, Department] =
    //   com.peknight.codec.Decoder.derived[Id, Query, Cursor[Query], DecodingFailure, Department]
    // given departmentsDecoder: com.peknight.codec.Decoder[Id, Cursor[Query], DecodingFailure, List[Department]] =
    //   com.peknight.codec.Decoder.decodeList[Id, Query, Department]
    // given companyDecoder: com.peknight.codec.Decoder[Id, Cursor[Query], DecodingFailure, Company] =
    //   com.peknight.codec.Decoder.derived[Id, Query, Cursor[Query], DecodingFailure, Company]
    // val company = Company("Pek", List(
    //   Department("X", List(Employee("A", 20, List(Apple, Pear)), Employee("B", 25, List(Pear, Peach)))),
    //   Department("Y", List(Employee("C", 30, List(Apple, Peach)), Employee("D", 35, List(Apple))))
    // ))
    // assert(parse[Company](company.toQueryString).exists(_ == company))
  }
end QueryParserFlatSpec
