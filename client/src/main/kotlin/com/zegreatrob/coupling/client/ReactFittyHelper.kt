package com.zegreatrob.coupling.client

import org.w3c.dom.Node
import kotlin.js.json

@JsModule("fitty")
@JsNonModule
private external val fitty: dynamic

fun Node.fitHeaderNode(maxFontHeight: Double, minFontHeight: Double) {
    fitty.default(this, json(
            "maxSize" to maxFontHeight,
            "minSize" to minFontHeight,
            "multiLine" to true
    )).unsafeCast<Unit>()
}