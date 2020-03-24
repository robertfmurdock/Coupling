import mu.KLogger
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration.LOG_LEVEL
import mu.KotlinLoggingLevel

val logger: KLogger = KotlinLogging.logger("TestAdapter")
    .also { it.setLogLevel() }

private fun KLogger.setLogLevel() {
    LOG_LEVEL = KotlinLoggingLevel.DEBUG
    warn { "Setting log level to debug." }
}
