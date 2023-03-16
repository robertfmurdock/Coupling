package com.zegreatrob.coupling.action

import com.soywiz.klock.PerformanceCounter
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.microseconds
import com.zegreatrob.testmints.action.Action

interface ActionLoggingSyntax : LoggingSyntax, TraceIdSyntax {

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
        val start = PerformanceCounter.microseconds
        val result = block()
        val end = PerformanceCounter.microseconds
        val duration = (end - start).microseconds
        logEnd(className, duration)
        return result
    }

    private fun logStart(className: String?) = logger.info {
        mapOf(
            "action" to className,
            "type" to "Start",
            "traceId" to traceId,
        )
    }

    private fun logEnd(className: String?, duration: TimeSpan) = logger.info {
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
