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
    given configuration: CodecConfiguration = CodecConfiguration()
    val iceCream = IceCream("rua", 7, true)
    val tuple = Tuple.fromProductTyped(iceCream)

    val monad: Monad[Id] = Monad[Id]
    println(monad)
    val cursorType: CursorType.Aux[Cursor[Query], Query] = CursorType[Cursor[Query]].asInstanceOf
    println(cursorType)
    val objectType: ObjectType[Query] = ObjectType[Query]
    println(objectType)
    val nullType: NullType[Query] = NullType[Query]
    println(nullType)
    val failure: Migration[DecodingFailure, DecodingFailure] = summon[Migration[DecodingFailure, DecodingFailure]]
    println(failure)
    val stringDecoder: Decoder[String] = Decoder[String]
    println(stringDecoder)
    val stringOptionDecoder: Decoder[Option[String]] = Decoder[Option[String]]
    println(stringOptionDecoder)
    val instances: Generic.Instances[Decoder, IceCream] = Generic.Product.Instances[Decoder, IceCream]
    println(instances)
    // val decoder: Decoder[IceCream] =
    //   com.peknight.codec.Decoder.derived[Id, Query, Cursor[Query], DecodingFailure, IceCream](using configuration)(using
    //     monad, cursorType, objectType, nullType, failure, stringDecoder, stringOptionDecoder, instances
    //   )
    // println(decoder)
  }
end QueryFlatSpec
