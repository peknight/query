package com.peknight.query.http4s.ops

import com.peknight.query.Query
import com.peknight.query.configuration.Configuration

object QueryOps:
  def toHttp4sQuery(query: Query)(using Configuration): org.http4s.Query =
    org.http4s.Query.fromVector(query.pairs.toVector)

end QueryOps
