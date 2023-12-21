package com.peknight.query

import com.peknight.codec.configuration.given
import com.peknight.codec.cursor.Cursor
import com.peknight.query.codec.id.{Decoder, Encoder}
import org.scalatest.flatspec.AnyFlatSpec

class QueryFlatSpec extends AnyFlatSpec:
  "Query Codec" should "succeed" in {
    val iceCream = IceCream("rua", 7, true)
    val tuple = Tuple.fromProductTyped(iceCream)
    assert(Decoder[IceCream].decodeS(Encoder[IceCream].encode(iceCream)).exists(_ == iceCream))
    assert(Decoder[(String, Int, Boolean)].decodeS(Encoder[(String, Int, Boolean)].encode(tuple)).exists(_ == tuple))
  }
end QueryFlatSpec
