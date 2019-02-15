package com.zegreatrob.coupling.common

import com.soywiz.klock.PerformanceCounter
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.microseconds
import mu.KotlinLogging

interface Action

interface ActionLoggingSyntax {
    companion object {
        private val logger = KotlinLogging.logger("ActionLogger")
    }

    fun <I : Action, O> I.log(block: (I) -> O) = logBlock { block(this) }

    suspend fun <I : Action, O> I.logAsync(block: suspend (I) -> O) = logBlock { block(this) }

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

    private fun logStart(className: String?) = logger.info { mapOf("action" to className, "type" to "Start") }

    private fun logEnd(className: String?, duration: TimeSpan) =
            logger.info { mapOf("action" to className, "type" to "End", "duration" to "$duration") }

    private fun logException(exception: Exception, className: String?) =
            logger.info(exception) { mapOf("action" to className, "type" to "End") }

}

