@file:JsModule("graphiql")
package com.zegreatrob.coupling.client.tribe

import react.ElementType
import react.Props
import kotlin.js.Json
import kotlin.js.Promise

external val GraphiQL: ElementType<GraphiQLProps>

external interface GraphiQLProps : Props {
    var editorTheme: String
    var fetcher: (graphQlParams: Json) -> Promise<String>
}
