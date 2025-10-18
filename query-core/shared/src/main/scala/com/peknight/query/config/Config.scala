package com.peknight.query.config

import cats.data.NonEmptyList
import com.peknight.codec.path.PathToRoot

trait Config[K]:
  def toKey(pathToRoot: PathToRoot): NonEmptyList[K]
end Config
