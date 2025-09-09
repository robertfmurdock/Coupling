package com.zegreatrob.coupling.client.components.external.fitty

import js.globals.globalThis
import web.html.HTMLElement
import kotlin.js.json

@JsModule("fitty")
private external val fitty: dynamic

fun HTMLElement.fitty(maxFontHeight: Double, minFontHeight: Double, multiLine: Boolean) {
    if (globalThis["IS_JSDOM"] == true) {
        return
    }
    fitty(
        this,
        json(
            "maxSize" to maxFontHeight,
            "minSize" to minFontHeight,
            "multiLine" to multiLine,
        ),
    ).unsafeCast<Unit>()
}
