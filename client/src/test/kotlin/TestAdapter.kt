import mu.KotlinLogging
import mu.KotlinLoggingLevel
import mu.LOG_LEVEL

private val logger = KotlinLogging.logger("TestAdapter")

@Suppress("unused")
@JsName("setLogLevel")
fun setLogLevel() {
    LOG_LEVEL = KotlinLoggingLevel.DEBUG
    logger.warn { "Setting log level to debug." }
}