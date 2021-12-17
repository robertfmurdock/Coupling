package com.zegreatrob.coupling.client.external.reactmarkdown

import kotlinext.js.jso
import react.RBuilder

fun RBuilder.markdown(source: String) = child(reactMarkdown, jso()) {
    +source
}