package com.zegreatrob.coupling.client.external.reactmarkdown

import kotlinext.js.jsObject
import react.RBuilder

fun RBuilder.markdown(source: String) = child(reactMarkdown, jsObject { }) {
    +source
}