package com.peknight.query.config

import cats.data.NonEmptyList
import com.peknight.codec.path.PathToRoot

trait Config[K]:
  def apply: PathToRoot => NonEmptyList[K]
end Config
