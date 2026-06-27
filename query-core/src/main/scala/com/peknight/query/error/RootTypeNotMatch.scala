package com.peknight.query.error

import com.peknight.error.parse.ParsingFailure

object RootTypeNotMatch extends ParsingFailure:
  override def lowPriorityLabelMessage(label: String): Option[String] =
    Some(s"$label: root type not match")
end RootTypeNotMatch
