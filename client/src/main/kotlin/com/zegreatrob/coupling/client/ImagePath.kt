package com.zegreatrob.coupling.client

fun imagePath(modulePath: String) = kotlinext.js.require("images/$modulePath.png").default.unsafeCast<String>()