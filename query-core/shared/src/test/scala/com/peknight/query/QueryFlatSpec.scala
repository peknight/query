package com.peknight.query

import cats.{Id, Monad}
import com.peknight.codec.configuration.CodecConfiguration
import com.peknight.query.codec.id.{Decoder, Encoder}
import org.scalatest.flatspec.AnyFlatSpec
import com.peknight.codec.cursor.{Cursor, CursorType}
import com.peknight.codec.error.DecodingFailure
import com.peknight.codec.sum.{ObjectType, NullType}
import com.peknight.generic.Generic
import com.peknight.generic.migration.id.Migration

class QueryFlatSpec extends AnyFlatSpec:
  "Query Codec" should "succeed" in {
    val configuration: CodecConfiguration = CodecConfiguration()
    val iceCream = IceCream("rua", 7, true)
    val tuple = Tuple.fromProductTyped(iceCream)
    val encoder: Encoder[IceCream] = com.peknight.codec.Encoder.derived(using configuration)
    val decoder: Decoder[IceCream] = com.peknight.codec.Decoder.derived(using configuration)
    val tupleEncoder: Encoder[(String, Int, Boolean)] = Encoder[(String, Int, Boolean)]
    val tupleDecoder: Decoder[(String, Int, Boolean)] = Decoder[(String, Int, Boolean)]
    println(decoder.decodeS(encoder.encode(iceCream)))
    println(tupleDecoder.decodeS(tupleEncoder.encode(tuple)))
  }
end QueryFlatSpec
