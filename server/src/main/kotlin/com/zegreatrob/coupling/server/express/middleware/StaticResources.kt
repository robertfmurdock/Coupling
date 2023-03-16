package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.external.express.static
import kotlin.js.json

fun staticResourcesPublic() = static(
    resourcePath("public"),
    json("extensions" to arrayOf("json")),
)
