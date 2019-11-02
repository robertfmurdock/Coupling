import com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration.LOG_LEVEL
import mu.KotlinLoggingLevel

private val logger = KotlinLogging.logger("TestAdapter")

@Suppress("unused")
@JsName("setLogLevel")
fun setLogLevel() {
    LOG_LEVEL = KotlinLoggingLevel.DEBUG
    logger.warn { "Setting log level to debug." }

    @Suppress("UNUSED_VARIABLE")
    val reporter = JasmineJsonLoggingReporter()

    js("jasmine.getEnv().addReporter(reporter)")
}


