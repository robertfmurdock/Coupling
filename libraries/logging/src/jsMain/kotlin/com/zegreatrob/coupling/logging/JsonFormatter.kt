package com.zegreatrob.coupling.logging

import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.KLoggingEvent
import kotlinx.serialization.json.Json
import kotlin.time.Clock

data object JsonFormatter : Formatter {

    override fun formatMessage(loggingEvent: KLoggingEvent): String = loggingEvent.formatMessage()

    private fun KLoggingEvent.formatMessage() = Json.encodeToString(
        Message.serializer(),
        Message(
            level = level.name,
            name = loggerName,
            message = message,
            properties = payload?.mapValues { (_, value) -> value.toString() },
            timestamp = Clock.System.now().toString(),
            marker = marker?.getName(),
            stackTrace = cause.throwableToString(),
        ),
    )

    private fun Throwable?.throwableToString(): List<String> {
        if (this == null) {
            return emptyList()
        }
        var msg = emptyList<String>()
        var current = this
        while (current != null && current.cause != current) {
            msg = msg + ", Caused by: '${current.message}'"
            current = current.cause
        }
        return msg
    }
}
