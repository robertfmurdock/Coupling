package com.zegreatrob.coupling.common

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import kotlin.reflect.KClass

interface Action

interface ActionLoggingSyntax {
    companion object {
        private val logger = KotlinLogging.logger("ActionLogger")
        private fun Message.logInfo() = logger.info { Json.stringify(Message.serializer(), this) }
        private fun Message.logInfo(exception: Exception) = logger.info(exception) { Json.stringify(Message.serializer(), this) }
    }

    fun <I : Action, O> I.log(block: (I) -> O) = LogContext(this::class).runBlock { block(this) }

    suspend fun <I : Action, O> I.logAsync(block: suspend (I) -> O) = LogContext(this::class).runBlock { block(this) }

    private data class LogContext(val actionClass: KClass<out Any>, val start: DateTime = DateTime.now()) {

        val className get() = actionClass.simpleName

        init {
            start()
        }

        private fun start() = Message(action = className, type = "Start", timestamp = start.logFormat())
                .logInfo()

        inline fun <O> runBlock(block: () -> O) = try {
            block().also { close() }
        } catch (exception: Exception) {
            closeExceptionally(exception)
            throw exception
        }

        fun close() = DateTime.now()
                .let { end ->
                    logEnd(end = end, duration = end - start)
                }

        fun closeExceptionally(exception: Exception) = DateTime.now()
                .let { end ->
                    Message(action = className, type = "End", duration = "${end - start}", timestamp = end.logFormat())
                            .logInfo(exception)
                }

        private fun logEnd(end: DateTime, duration: TimeSpan) =
                Message(action = className, type = "End", duration = "$duration", timestamp = end.logFormat())
                        .logInfo()
    }

}

private fun DateTime.logFormat() = toString(DateFormat.FORMAT1)

@Serializable
private data class Message(
        val action: String?,
        val type: String,
        val timestamp: String,
        val duration: String? = null
)