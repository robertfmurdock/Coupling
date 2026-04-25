package com.zegreatrob.coupling.testlogging

import com.zegreatrob.coupling.logging.initializeLogging
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.js.json
import kotlin.time.Clock
import kotlin.time.Instant

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
    private var lastStart: Instant? = null

    init {
        initialize()
    }

    @Suppress("unused")
    @JsName("specStarted")
    fun specStarted(result: dynamic) {
        val fullName = result.fullName.unsafeCast<String>()
        val testName = result.description?.unsafeCast<String>() ?: fullName
        val suiteName = fullName.removeSuffix(" $testName").ifBlank { fullName }
        val testId = result.id?.unsafeCast<String>()
        setCurrentTestContext(suiteName = suiteName, testName = testName, testId = testId)
        logger.info {
            CanonicalTestLogEvents.phaseMap(
                phase = "setup-start",
                additional = mapOf(
                    "test_suite" to suiteName,
                    "test_name" to testName,
                    "test_id" to testId,
                ),
            )
        }
        startTest(testName = testName, suiteName = suiteName, testId = testId)
    }

    @Suppress("unused")
    @JsName("specDone")
    fun specDone(result: dynamic) {
        val fullName = result.fullName.unsafeCast<String>()
        val testName = result.description?.unsafeCast<String>() ?: fullName
        val suiteName = fullName.removeSuffix(" $testName").ifBlank { fullName }
        endTest(
            testName = testName,
            suiteName = suiteName,
            testId = result.id?.unsafeCast<String>(),
            status = result.status.unsafeCast<String>(),
            failed = result.failedExpectations.unsafeCast<Array<dynamic>>(),
        )
        clearCurrentTestContext()
    }

    @JsName("startTest")
    fun startTest(testName: String, suiteName: String, testId: String?) = logger.info {
        mapOf(
            "type" to "TestStart",
            "suite" to suiteName,
            "test" to testName,
            "test_id" to testId,
        )
    }
        .also { lastStart = Clock.System.now() }

    @JsName("endTest")
    fun endTest(testName: String, suiteName: String, testId: String?, status: String, failed: Array<dynamic>?) {
        val duration = lastStart?.let { Clock.System.now() - it }
        logger.info {
            mapOf(
                "type" to "TestEnd",
                "suite" to suiteName,
                "test" to testName,
                "test_id" to testId,
                "status" to status,
                "duration" to "$duration",
                "failures" to failed?.joinToString("\n", "\n") { "message: ${it.message} \nstack: ${it.stack}" },
            )
        }
            .also { lastStart = null }
    }

    private fun setCurrentTestContext(suiteName: String, testName: String, testId: String?) {
        val globalThis = js("globalThis")
        globalThis.__couplingCurrentTest = json(
            "suite" to suiteName,
            "test" to testName,
            "testId" to testId,
        )
    }

    private fun clearCurrentTestContext() {
        js("globalThis.__couplingCurrentTest = null")
    }
}
