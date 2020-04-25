package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.on_finished.onFinished
import com.zegreatrob.coupling.server.logRequestAsync

fun logRequests(): Handler = { request, response, next ->
    logRequestAsync(request, response) { callback ->
        onFinished(
            response,
            callback
        )
    }
        .also { next() }
}