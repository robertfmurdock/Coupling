package com.zegreatrob.coupling.server

import com.soywiz.klock.measureTime
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger by lazy { KotlinLogging.logger("RequestLogger") }

@Suppress("unused")
@JsName("logRequestAsync")
fun logRequestAsync(request: Request, response: Response, block: (() -> Unit) -> Unit) = GlobalScope.launch {
    val duration = measureTime {
        val deferred = CompletableDeferred<Unit>()

        block { deferred.complete(Unit) }

        deferred.await()
    }

    val url = request.originalUrl ?: request.url

    logger.info {
        mapOf(
            "method" to request.method,
            "url" to url,
            "statusCode" to "${response.statusCode}",
            "contentLength" to response["content-length"]?.toString(),
            "duration" to "$duration"
        )
    }
}
