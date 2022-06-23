package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.components.waitForAsyncReactComponent
import kotlinx.browser.window
import org.w3c.dom.get
import react.ElementType
import react.FC
import react.Props
import kotlin.js.Json
import kotlin.js.Promise

val GraphiQL: ElementType<GraphiQLProps> = FC { props ->
    waitForAsyncReactComponent({ window["GraphiQL"].unsafeCast<ElementType<Props>?>() }) { component ->
        component { +props }
    }
}

external interface GraphiQLProps : Props {
    var editorTheme: String
    var fetcher: (graphQlParams: Json) -> Promise<String>
}
