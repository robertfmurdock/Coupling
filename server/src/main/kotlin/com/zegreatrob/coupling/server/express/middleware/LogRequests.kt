package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.onfinished.onFinished

fun logRequests(): Handler = { request, response, next ->
    logRequestAsync(
        request,
        response
    ) { callback ->
        onFinished(
            response,
            callback
        )
    }
        .also { next() }
}
