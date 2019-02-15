
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.logging.initializeLogging
import mu.KotlinLogging

@JsName("JasmineJsonLoggingReporter")
class JasmineJsonLoggingReporter {
    private val logger by lazy { KotlinLogging.logger("JasmineJsonLoggingReporter") }
    private var lastStart: DateTime? = null

    init {
        initializeLogging(true)
    }

    @Suppress("unused")
    @JsName("specStarted")
    fun specStarted(result: dynamic) {
        startTest(result.fullName.unsafeCast<String>())
    }

    @Suppress("unused")
    @JsName("specDone")
    fun specDone(result: dynamic) = endTest(result.fullName.unsafeCast<String>())

    private fun startTest(testName: String) = logger.info { mapOf("type" to "TestStart", "test" to testName) }
            .also { lastStart = DateTime.now() }

    private fun endTest(testName: String) {
        val duration = lastStart?.let { DateTime.now() - it }
        logger.info { mapOf("type" to "TestEnd", "test" to testName, "duration" to "$duration") }
                .also { lastStart = null }
    }
}