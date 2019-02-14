package com.zegreatrob.coupling.common

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import mu.KotlinLogging
import kotlin.reflect.KClass

interface Action

interface ActionLoggingSyntax {
    companion object {
        private val logger = KotlinLogging.logger("ActionLogger")
    }

    fun <I : Action, O> I.log(block: (I) -> O) = LogContext(this::class).runBlock { block(this) }

    suspend fun <I : Action, O> I.logAsync(block: suspend (I) -> O) = LogContext(this::class).runBlock { block(this) }

    private data class LogContext(val actionClass: KClass<out Any>, val start: DateTime = DateTime.now()) {

        val className get() = actionClass.simpleName

        init {
            start()
        }

        private fun start() = logger.info { mapOf("action" to className, "type" to "Start") }

        inline fun <O> runBlock(block: () -> O) = try {
            block().also { close() }
        } catch (exception: Exception) {
            closeExceptionally(exception)
            throw exception
        }

        fun close() = logEnd(duration = DateTime.now() - start)

        fun closeExceptionally(exception: Exception) = DateTime.now()
                .let { end ->
                    logger.info(exception) { mapOf("action" to className, "type" to "End", "duration" to "${end - start}") }
                }

        private fun logEnd(duration: TimeSpan) =
                logger.info { mapOf("action" to className, "type" to "End", "duration" to "$duration") }
    }

}
