package com.zegreatrob.coupling.server.external.express_graphql

import com.zegreatrob.coupling.server.external.express.Router
import kotlin.js.Json

@JsModule("express-graphql")
@JsNonModule
external val graphqlHTTP: (Json) -> Router