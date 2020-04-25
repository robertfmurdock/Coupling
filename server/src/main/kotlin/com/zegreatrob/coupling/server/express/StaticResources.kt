package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.external.express.static
import kotlin.js.json

fun staticResources() = static(
    resourcePath("public"),
    json("extensions" to arrayOf("json"))
)