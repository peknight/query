package com.peknight.query.option

enum OptionKeyType(val prefix: Option[String]) derives CanEqual:
  case LongOption extends OptionKeyType(Some("--"))
  case ShortOption extends OptionKeyType(Some("-"))
  case NonStandardOption extends OptionKeyType(Some("-"))
  case BSDOption extends OptionKeyType(None)
end OptionKeyType
