package com.zegreatrob.coupling.testlogging

import com.zegreatrob.coupling.logging.initializeLogging
import korlibs.time.DateTime
import mu.KotlinLogging

@Suppress("unused")
@JsName("JasmineJsonLoggingReporter")
@JsExport
class JasmineJsonLoggingReporter {

    companion object {
        fun initialize() {
            initializeLogging(true)
            JsonLoggingTestMintsReporter.initialize()
        }
    }

    private val logger by lazy { KotlinLogging.logger("JasmineJsonLoggingReporter") }
    private var lastStart: DateTime? = null

    init {
        initialize()
    }

    @Suppress("unused")
    @JsName("specStarted")
    fun specStarted(result: dynamic) {
        startTest(result.fullName.unsafeCast<String>())
    }

    @Suppress("unused")
    @JsName("specDone")
    fun specDone(result: dynamic) = endTest(
        result.fullName.unsafeCast<String>(),
        result.status.unsafeCast<String>(),
        result.failedExpectations.unsafeCast<Array<dynamic>>(),
    )

    @JsName("startTest")
    fun startTest(testName: String) = logger.info { mapOf("type" to "TestStart", "test" to testName) }
        .also { lastStart = DateTime.now() }

    @JsName("endTest")
    fun endTest(testName: String, status: String, failed: Array<dynamic>?) {
        val duration = lastStart?.let { DateTime.now() - it }
        logger.info {
            mapOf(
                "type" to "TestEnd",
                "test" to testName,
                "status" to status,
                "duration" to "$duration",
                "failures" to failed?.joinToString("\n", "\n") { "message: ${it.message} \nstack: ${it.stack}" },
            )
        }
            .also { lastStart = null }
    }
}
