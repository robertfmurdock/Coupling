import com.soywiz.klock.DateTime
import mu.KotlinLogging
import mu.KotlinLoggingLevel
import mu.LOG_LEVEL

private val logger = KotlinLogging.logger("TestAdapter")

@Suppress("unused")
@JsName("setLogLevel")
fun setLogLevel() {
    LOG_LEVEL = KotlinLoggingLevel.DEBUG
    logger.warn { "Setting log level to debug." }

    @Suppress("UNUSED_VARIABLE")
    val reporter = JsonLoggingReporter()

    js("jasmine.getEnv().addReporter(reporter)")
}

class JsonLoggingReporter {
    private val logger = KotlinLogging.logger {}
    private var lastStart: DateTime? = null

    @Suppress("unused")
    @JsName("specStarted")
    fun specStarted(result: dynamic) = startTest(result.fullName.unsafeCast<String>())

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


