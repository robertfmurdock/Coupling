package com.zegreatrob.coupling.repository.dynamo

import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.measureTimedValue

private val theLogger by lazy { KotlinLogging.logger("DynamoLogger") }

interface DynamoLoggingSyntax {

    val logger get() = theLogger

    fun <I, O> I.log(className: String, block: (I) -> O) = logBlock(className) { block(this) }

    suspend fun <I, O> I.logAsync(className: String, block: suspend (I) -> O) = logBlock(className) { block(this) }

    private inline fun <O> logBlock(className: String, anotherBlock: () -> O): O {
        logStart(className)

        return try {
            runBlock(anotherBlock, className)
        } catch (exception: Exception) {
            logException(exception, className)
            throw exception
        }
    }

    private inline fun <O> runBlock(block: () -> O, className: String?): O {
        val (result, duration) = measureTimedValue(block)
        logEnd(className, duration)
        return result
    }

    private fun logStart(className: String?) = logger.info { mapOf("func" to className, "type" to "Start") }

    private fun logEnd(className: String?, duration: Duration) =
        logger.info { mapOf("func" to className, "type" to "End", "duration" to "$duration") }

    private fun logException(exception: Exception, className: String?) =
        logger.info(exception) { mapOf("func" to className, "type" to "End") }
}
