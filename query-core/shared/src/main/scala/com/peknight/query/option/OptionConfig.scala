package com.peknight.query.option

import cats.data.NonEmptyList
import com.peknight.codec.path.PathElem.ObjectKey
import com.peknight.codec.path.PathToRoot
import com.peknight.query.config.{ArrayOp, Config, PathOp}
import com.peknight.query.option.ArgumentStyle.SpaceSeparated
import com.peknight.query.option.OptionKey.{BSDOption, LongOption, NonStandardOption, ShortOption}
import spire.math.Interval

trait OptionConfig extends Config[OptionKey]:
  def transformKey: PathToRoot => List[OptionKey]
  def nonStandardOption: Boolean
  def argumentStyle: ArgumentStyle
  def argumentLength: Interval[Int]
  def toKeys(pathToRoot: PathToRoot): NonEmptyList[OptionKey] =
    NonEmptyList.fromList(transformKey(pathToRoot)).getOrElse {
      val key = toKey(pathToRoot)
      val argLen = pathToRoot.value.lastOption
        .map {
          case ObjectKey(keyName) if flagKeys.contains(keyName) => Interval.point(0)
          case _ => argumentLength
        }
        .getOrElse(argumentLength)
      val optionKey = {
        if nonStandardOption then
          if key.length == 1 then BSDOption(key.head, argumentStyle, argLen)
          else NonStandardOption(key, argumentStyle, argLen)
        else
          if key.length == 1 then ShortOption(key.head, argumentStyle, argLen)
          else LongOption(key, argumentStyle, argLen)
      }
      NonEmptyList.one(optionKey)
    }
end OptionConfig
object OptionConfig:
  private case class OptionConfig(
    transformKey: PathToRoot => List[OptionKey],
    nonStandardOption: Boolean,
    argumentStyle: ArgumentStyle,
    argumentLength: Interval[Int],
    lastArrayOp: ArrayOp,
    pathOp: PathOp,
    defaultKeys: List[String],
    flagKeys: List[String],
  ) extends com.peknight.query.option.OptionConfig

  def apply(
             keyMapper: PathToRoot => List[OptionKey] = _ => Nil,
             nonStandardOption: Boolean = false,
             argumentStyle: ArgumentStyle = SpaceSeparated,
             argumentLength: Interval[Int] = Interval.closed(0, 1),
             lastArrayOp: ArrayOp = ArrayOp.Empty,
             pathOp: PathOp = PathOp.PathString,
             defaultKeys: List[String] = Nil,
             flagKeys: List[String] = Nil
           ): com.peknight.query.option.OptionConfig =
    OptionConfig(keyMapper, nonStandardOption, argumentStyle, argumentLength, lastArrayOp, pathOp, defaultKeys,
      flagKeys)

  val default: com.peknight.query.option.OptionConfig = apply()
end OptionConfig
