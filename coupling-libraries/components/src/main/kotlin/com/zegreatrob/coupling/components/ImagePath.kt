package com.zegreatrob.coupling.components

fun pngPath(modulePath: String): String {
    return if (js("global.IS_JSDOM") == true) {
        modulePath
    } else
        kotlinext.js.require("images/$modulePath.png").unsafeCast<String>()
}

fun svgPath(modulePath: String) = kotlinext.js.require("images/$modulePath.svg").unsafeCast<String>()
