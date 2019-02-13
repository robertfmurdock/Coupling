package com.zegreatrob.coupling.common

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging


interface ActionLoggingSyntax {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Serializable
    private data class Message(
            val className: String?,
            val type: String,
            val timestamp: String,
            val duration: String? = null
    )

    fun <I : Any, O> I.log(block: (I) -> O): O {
        val start = logStart()
        return block(this)
                .also {
                    logEnd(start)
                }

    }

    suspend fun <I : Any, O> I.logAsync(block: suspend (I) -> O): O {
        val start = logStart()
        return block(this)
                .also {
                    logEnd(start)
                }

    }

    private fun <I : Any> I.logStart(): DateTime {
        val start = DateTime.now()
        Message(
                className = this::class.simpleName,
                type = "Start",
                timestamp = start.toString(DateFormat.FORMAT1)
        )
                .logInfo()
        return start
    }

    private fun Message.logInfo() {
        println(Json.stringify(Message.serializer(), this))
        logger.info { Json.stringify(Message.serializer(), this) }
        logger.info { "RIGHT HERE MAN" }
    }

    private fun <I : Any> I.logEnd(start: DateTime) {
        val end = DateTime.now()
        val duration = end - start
        Message(
                className = this::class.simpleName,
                type = "End",
                duration = duration.toString(),
                timestamp = end.toString(DateFormat.FORMAT1)
        )
                .logInfo()
    }
}
