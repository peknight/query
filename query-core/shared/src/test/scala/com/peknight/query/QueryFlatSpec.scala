package com.peknight.query

import com.peknight.codec.configuration.CodecConfiguration
import com.peknight.codec.cursor.Cursor
import com.peknight.query.codec.id.{Decoder, Encoder}
import org.scalatest.flatspec.AnyFlatSpec

class QueryFlatSpec extends AnyFlatSpec:
  "Query Codec" should "succeed" in {
    given configuration: CodecConfiguration = CodecConfiguration()
    val iceCream = IceCream("rua", 7, true)
    val tuple = Tuple.fromProductTyped(iceCream)
    val encoder: Encoder[IceCream] = Encoder[IceCream]
    val decoder: Decoder[IceCream] = Decoder[IceCream]
    val tupleEncoder: Encoder[(String, Int, Boolean)] = Encoder[(String, Int, Boolean)]
    val tupleDecoder: Decoder[(String, Int, Boolean)] = Decoder[(String, Int, Boolean)]
    println(decoder.decodeS(encoder.encode(iceCream)))
    println(tupleDecoder.decodeS(tupleEncoder.encode(tuple)))
  }
end QueryFlatSpec
