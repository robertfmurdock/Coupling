@file:JsModule("express-graphql")


package com.zegreatrob.coupling.server.external.express_graphql

import com.zegreatrob.coupling.server.external.express.Router
import kotlin.js.Json

external val graphqlHTTP: (Json) -> Router
