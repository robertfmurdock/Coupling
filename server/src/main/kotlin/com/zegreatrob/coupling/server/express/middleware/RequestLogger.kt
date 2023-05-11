package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.time.measureTime

private val logger by lazy { KotlinLogging.logger("RequestLogger") }

fun logRequestAsync(request: Request, response: Response, block: (() -> Unit) -> Unit) = request.scope.launch {
    val url = request.originalUrl ?: request.url

    logger.debug {
        mapOf("method" to request.method, "url" to url, "message" to "STARTING", "traceId" to "${request.traceId}")
    }

    val duration = measureTime {
        val deferred = CompletableDeferred<Unit>()
        block { deferred.complete(Unit) }
        deferred.await()
    }

    logger.info {
        mapOf(
            "method" to request.method,
            "url" to url,
            "statusCode" to "${response.statusCode}",
            "contentLength" to response["content-length"]?.toString(),
            "duration" to "$duration",
            "traceId" to "${request.traceId}",
        )
    }
}
