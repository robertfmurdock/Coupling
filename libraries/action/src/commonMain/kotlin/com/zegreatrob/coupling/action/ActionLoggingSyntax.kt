package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.Action
import kotlin.time.Duration
import kotlin.time.measureTimedValue

interface ActionLoggingSyntax : LoggingProvider, TraceIdProvider {

    fun <I : Action, O> log(action: I, block: (I) -> O) = action.logBlock { block(action) }

    suspend fun <I : Action, O> logAsync(action: I, block: suspend (I) -> O) = action.logBlock { block(action) }

    private inline fun <I : Action, O> I.logBlock(anotherBlock: () -> O): O {
        val className = this::class.simpleName
        logStart(className)

        return try {
            runBlock(anotherBlock, className)
        } catch (exception: Exception) {
            logException(exception, className)
            throw exception
        }
    }

    private inline fun <O> runBlock(block: () -> O, className: String?): O {
        val result = measureTimedValue { block() }
        val duration = result.duration
        logEnd(className, duration)
        return result.value
    }

    private fun logStart(className: String?) = logger.info {
        mapOf(
            "action" to className,
            "type" to "Start",
            "traceId" to traceId,
        )
    }

    private fun logEnd(className: String?, duration: Duration) = logger.info {
        mapOf(
            "action" to className,
            "type" to "End",
            "duration" to "$duration",
            "traceId" to traceId,
        )
    }

    private fun logException(exception: Exception, className: String?) = logger.info(exception) {
        mapOf(
            "action" to className,
            "type" to "End",
            "traceId" to traceId,
        )
    }
}
