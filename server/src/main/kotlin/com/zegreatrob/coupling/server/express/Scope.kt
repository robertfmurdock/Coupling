package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.external.express.Handler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus

fun scope(): Handler = { request, _, next ->
    request.asDynamic().scope = mainScope(request.path)
    next()
}

private fun mainScope(path: String) = MainScope() + CoroutineName(path)