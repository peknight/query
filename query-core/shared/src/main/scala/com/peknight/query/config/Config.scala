package com.peknight.query.config

import cats.data.NonEmptyList
import com.peknight.codec.path.PathElem.{ArrayIndex, ObjectKey}
import com.peknight.codec.path.PathToRoot
import com.peknight.query.config.ArrayOp.{Brackets, Empty, Index}

trait Config[K]:
  def lastArrayOp: ArrayOp
  def pathOp: PathOp
  def defaultKeys: List[String]
  def flagKeys: List[String]
  def toKeys(pathToRoot: PathToRoot): NonEmptyList[K]

  def toKey(pathToRoot: PathToRoot): String =
    val elems = pathToRoot.value
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
end Config
