package com.zegreatrob.coupling.server.external.graphql

import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.Json

typealias Resolver = (Json, Json, Request, Json) -> Any?
