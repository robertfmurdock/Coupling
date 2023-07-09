@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.parse5

import org.w3c.dom.Document

@JsModule("parse5")
external val parse5: Parse5

external interface Parse5 {
    fun parse(content: String): Document
}
