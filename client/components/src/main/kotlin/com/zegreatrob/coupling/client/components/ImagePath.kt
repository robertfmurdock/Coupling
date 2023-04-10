package com.zegreatrob.coupling.client.components

fun pngPath(modulePath: String): String = if (js("global.IS_JSDOM") == true) {
    "pngPath/$modulePath"
} else {
    kotlinext.js.require("images/$modulePath.png").unsafeCast<String>()
}

fun svgPath(modulePath: String): String = if (js("global.IS_JSDOM") == true) {
    "pngPath/$modulePath"
} else {
    kotlinext.js.require("images/$modulePath.svg").unsafeCast<String>()
}
