@file:JsModule("@graphiql/toolkit")
package com.zegreatrob.coupling.client.tribe

import kotlin.js.Json
import kotlin.js.Promise

external fun createGraphiQLFetcher(options: Json):(graphQlParams: Json) -> Promise<dynamic>
