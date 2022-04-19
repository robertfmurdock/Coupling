package com.zegreatrob.coupling.client

fun pngPath(modulePath: String) = kotlinext.js.require("images/$modulePath.png").unsafeCast<String>()
fun svgPath(modulePath: String) = kotlinext.js.require("images/$modulePath.svg").unsafeCast<String>()
