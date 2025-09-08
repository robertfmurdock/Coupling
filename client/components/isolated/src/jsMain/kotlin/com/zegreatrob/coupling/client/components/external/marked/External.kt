@file:JsModule("marked")

package com.zegreatrob.coupling.client.components.external.marked

external val marked: Marked

external interface Marked {
    fun parse(markdown: String): String
}
