package com.zegreatrob.coupling.server.express.middleware

fun favicon() = com.zegreatrob.coupling.server.external.serve_favicon.favicon(
    resourcePath("public/images/favicon.ico")
)