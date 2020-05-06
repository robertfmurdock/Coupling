package com.zegreatrob.coupling.server.express.middleware

fun viewResources() = arrayOf(
    resourcePath("public"),
    resourcePath("views")
)