package com.zegreatrob.coupling.client

fun pngPath(modulePath: String) = kotlinext.js.require("images/$modulePath.png").default.unsafeCast<String>()