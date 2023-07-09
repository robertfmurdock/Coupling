package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.serverScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.plus

fun scope(): Handler = { request, _, next ->
    request.asDynamic().scope = pathScope(request.path)
    next()
}

private fun pathScope(path: String) = serverScope + CoroutineName(path)
