package com.zegreatrob.coupling.client.components.external.fitty

import web.html.HTMLElement
import kotlin.js.json

fun HTMLElement.fitty(maxFontHeight: Double, minFontHeight: Double, multiLine: Boolean) {
    if (js("global.IS_JSDOM") == true) {
        return
    }
    val fitty = kotlinext.js.require<dynamic>("fitty")
    fitty.default(
        this,
        json(
            "maxSize" to maxFontHeight,
            "minSize" to minFontHeight,
            "multiLine" to multiLine,
        ),
    ).unsafeCast<Unit>()
}
