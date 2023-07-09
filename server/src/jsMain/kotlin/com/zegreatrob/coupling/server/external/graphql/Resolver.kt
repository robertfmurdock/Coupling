package com.zegreatrob.coupling.server.external.graphql

import com.zegreatrob.coupling.server.express.route.CouplingContext
import kotlin.js.Json

typealias Resolver = (Json, Json, CouplingContext, Json) -> Any?
