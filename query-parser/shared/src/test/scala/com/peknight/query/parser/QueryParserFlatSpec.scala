package com.peknight.query.parser

import cats.Id
import com.peknight.codec.configuration.given
import com.peknight.query.codec.id.Encoder
import org.scalatest.flatspec.AnyFlatSpec

class QueryParserFlatSpec extends AnyFlatSpec:

  "Query Parser" should "succeed with ice cream" in {
    val iceCream = IceCream("r&u=a+", 7, true)
    val tuple: (String, Int, Boolean) = Tuple.fromProductTyped(iceCream)
    assert(parse[Id, IceCream](Encoder[IceCream].encode(iceCream).mkString).exists(_ == iceCream))
    assert(parse[Id, (String, Int, Boolean)](Encoder[(String, Int, Boolean)].encode(tuple).mkString).exists(_ == tuple))
  }

  "Query Parser" should "succeed with company" in {
    // import com.peknight.codec.cursor.Cursor
    // import com.peknight.codec.error.DecodingFailure
    // import com.peknight.query.Query
    // import com.peknight.query.parser.Fruit.{Apple, Peach, Pear}
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
    // assert(parse[Id, Company](Encoder[Company].encode(company).mkString).exists(_ == company))
  }
end QueryParserFlatSpec
