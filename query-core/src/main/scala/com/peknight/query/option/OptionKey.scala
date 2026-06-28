package com.peknight.query.option

import cats.Show
import cats.syntax.eq.*
import com.peknight.query.option.ArgumentStyle.SpaceSeparated
import spire.math.Interval

sealed trait OptionKey:
  def key: String
  def keyType: OptionKeyType
  def argumentStyle: ArgumentStyle
  def argumentLength: Interval[Int]
  def combinable: Boolean
end OptionKey
object OptionKey:
  case class LongOption(key: String, argumentStyle: ArgumentStyle = SpaceSeparated,
                        private val argLen: Interval[Int] = Interval.point(1)) extends OptionKey:
    def keyType: OptionKeyType = OptionKeyType.LongOption
    def argumentLength: Interval[Int] = argLen & Interval.atOrAbove(0)
    def combinable: Boolean = false
  end LongOption
  case class ShortOption(private val keyChar: Char, argumentStyle: ArgumentStyle = SpaceSeparated,
                         private val argLen: Interval[Int] = Interval.point(1),
                         private val canBeCombined: Boolean = true) extends OptionKey:
    def key: String = s"$keyChar"
    def keyType: OptionKeyType = OptionKeyType.ShortOption
    def argumentLength: Interval[Int] = argLen & Interval.atOrAbove(0)
    def combinable: Boolean = canBeCombined && argumentLength === Interval.point(0)
  end ShortOption
  case class NonStandardOption(key: String, argumentStyle: ArgumentStyle = SpaceSeparated,
                               private val argLen: Interval[Int] = Interval.point(1)) extends OptionKey:
    def keyType: OptionKeyType = OptionKeyType.NonStandardOption
    def argumentLength: Interval[Int] = argLen & Interval.atOrAbove(0)
    def combinable: Boolean = false
  end NonStandardOption
  case class BSDOption(private val keyChar: Char, argumentStyle: ArgumentStyle = SpaceSeparated,
                       private val argLen: Interval[Int] = Interval.point(0),
                       private val canBeCombined: Boolean = true) extends OptionKey:
    def key: String = s"$keyChar"
    def keyType: OptionKeyType = OptionKeyType.BSDOption
    def argumentLength: Interval[Int] = argLen & Interval.atOrAbove(0)
    def combinable: Boolean = canBeCombined && argumentLength === Interval.point(0)
  end BSDOption
  case object None extends OptionKey:
    def key: String = ""
    def keyType: OptionKeyType = OptionKeyType.None
    def argumentStyle: ArgumentStyle = ArgumentStyle.NoArgument
    def argumentLength: Interval[Int] = Interval.point(0)
    def combinable: Boolean = false
  end None
  given showOptionKey: Show[OptionKey] =
    Show.fromToString[OptionKey]
end OptionKey

