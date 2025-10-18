package com.peknight.query.config

import cats.data.NonEmptyList
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.PathToRoot
import com.peknight.query.config.ArrayOp.{Brackets, Empty, Index}

trait QueryConfig extends Config[String]:
  def lastArrayOp: ArrayOp
  def pathOp: PathOp
  def defaultKeys: List[String]
  def flagKeys: List[String]
  def toKey(pathToRoot: PathToRoot): NonEmptyList[String] =
    val elems = pathToRoot.value
    val key =
      if elems.isEmpty then ""
      else if elems.length == 1 then
        elems.head match
          case ObjectKey(keyName) =>
            if defaultKeys.contains(keyName) then ""
            else keyName
          case ArrayIndex(index) =>
            lastArrayOp match
              case Index => s"$index"
              case Brackets => "[]"
              case Empty => ""
      else
        val head = elems.head match
          case ObjectKey(keyName) => keyName
          case ArrayIndex(index) => s"$index"
        val last = elems.last match
          case ObjectKey(keyName) if defaultKeys.contains(keyName) => ""
          case ObjectKey(keyName) =>
            pathOp match
              case PathOp.PathString => s".$keyName"
              case PathOp.Brackets => s"[$keyName]"
          case ArrayIndex(index) =>
            lastArrayOp match
              case Index => s"[$index]"
              case Brackets => "[]"
              case Empty => ""
        if elems.length == 2 then s"$head$last"
        else
          val m = elems.tail.init
          m.foldLeft(new StringBuilder(m.size * 5).append(head)) {
            case (sb, ObjectKey(keyName)) =>
              pathOp match
                case PathOp.PathString => sb.append(".").append(keyName)
                case PathOp.Brackets => sb.append("[").append(keyName).append("]")
            case (sb, ArrayIndex(index)) => sb.append("[").append(index.toString).append("]")
          }.append(last).toString
    NonEmptyList.one(key)

end QueryConfig
object QueryConfig:
  private case class QueryConfig(
    lastArrayOp: ArrayOp,
    pathOp: PathOp,
    defaultKeys: List[String],
    flagKeys: List[String]
  ) extends com.peknight.query.config.QueryConfig

  def apply(
    lastArrayOp: ArrayOp = ArrayOp.Brackets,
    pathOp: PathOp = PathOp.PathString,
    defaultKeys: List[String] = Nil,
    flagKeys: List[String] = Nil
  ): com.peknight.query.config.QueryConfig =
    QueryConfig(lastArrayOp, pathOp, defaultKeys, flagKeys)

  val default: com.peknight.query.config.QueryConfig = apply()
end QueryConfig
