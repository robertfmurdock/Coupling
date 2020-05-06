package com.zegreatrob.coupling.server.express.middleware

fun resourcePath(directory: String) = "${js("__dirname")}/../../../server/build/executable/$directory"
