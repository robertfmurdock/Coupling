package com.zegreatrob.coupling.client.components.external.marked

@JsModule("marked")
external val marked: Marked

external interface Marked {
    fun parse(markdown: String): String
}


fun parse(markdown: String): String = marked.parse(markdown)
