package com.zegreatrob.coupling.client.components

fun pngPath(modulePath: String): String = if (js("global.IS_JSDOM") == true) {
    "pngPath/$modulePath"
} else {
    require("images/$modulePath.png")
}

fun svgPath(modulePath: String): String = if (js("global.IS_JSDOM") == true) {
    "pngPath/$modulePath"
} else {
    require("images/$modulePath.svg")
}

external fun <T> require(module: String): T
