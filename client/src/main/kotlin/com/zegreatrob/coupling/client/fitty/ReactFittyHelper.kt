package com.zegreatrob.coupling.client.fitty

import org.w3c.dom.Node
import kotlin.js.json

@JsModule("fitty")

private external val fitty: dynamic

fun Node.fitty(maxFontHeight: Double, minFontHeight: Double, multiLine: Boolean) {
    fitty.default(
        this,
        json(
            "maxSize" to maxFontHeight,
            "minSize" to minFontHeight,
            "multiLine" to multiLine
        )
    ).unsafeCast<Unit>()
}
