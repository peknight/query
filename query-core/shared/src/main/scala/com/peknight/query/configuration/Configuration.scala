package com.peknight.query.configuration

import com.peknight.commons.string

trait Configuration:
  def lastArrayOp: ArrayOp
  def pathOp: PathOp
  def escapeCharacter: Char
  def escapeCharacters: Set[Char]
  def defaultKey: Option[String]
  def escape(input: String): String =
    val chars = (lastArrayOp, pathOp) match
      case (ArrayOp.Brackets, PathOp.PathString) => Set('.', '[', ']', escapeCharacter) ++ escapeCharacters
      case (_, PathOp.PathString) => Set('.', escapeCharacter) ++ escapeCharacters
      case _ => Set('[', ']', escapeCharacter) ++ escapeCharacters
    string.escape(input, escapeCharacter, chars)
end Configuration
object Configuration:
  private[this] case class Configuration(
    lastArrayOp: ArrayOp,
    pathOp: PathOp,
    escapeCharacter: Char,
    escapeCharacters: Set[Char],
    defaultKey: Option[String]
  ) extends com.peknight.query.configuration.Configuration

  def apply(
    lastArrayOp: ArrayOp = ArrayOp.Brackets,
    pathOp: PathOp = PathOp.PathString,
    escapeCharacter: Char = '\\',
    escapeCharacters: Set[Char] = Set('&', '='),
    defaultKey: Option[String] = None
  ): com.peknight.query.configuration.Configuration =
    Configuration(lastArrayOp, pathOp, escapeCharacter, escapeCharacters, defaultKey)
end Configuration
