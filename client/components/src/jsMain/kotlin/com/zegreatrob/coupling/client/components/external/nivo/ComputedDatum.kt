package com.zegreatrob.coupling.client.components.external.nivo

import kotlinx.js.JsPlainObject

@JsPlainObject
external interface ComputedDatum {
    val id: String
    val group: String
    val indexValue: Any
    val formattedValue: Any
}
