package com.peknight.query.option

import cats.Eq

enum ArgumentStyle derives CanEqual:
  case EqualsSeparated, SpaceSeparated, NoArgument
end ArgumentStyle
object ArgumentStyle:
  given eqArgumentStyle: Eq[ArgumentStyle] = Eq.fromUniversalEquals[ArgumentStyle]
end ArgumentStyle

