package com.zegreatrob.coupling.logging

import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.KLoggingEvent
import kotlinx.serialization.json.Json
import kotlin.time.Clock

data object JsonFormatter : Formatter {

    override fun formatMessage(loggingEvent: KLoggingEvent): String {
        val message = loggingEvent.formatMessage()
        appendToTestLog(message)
        return message
    }

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

    private fun appendToTestLog(message: String) {
        val logPath = js("typeof process !== 'undefined' && process.env ? process.env.COUPLING_TEST_LOG_PATH : null")
        if (logPath == null) {
            return
        }
        val hasNode = js("typeof process !== 'undefined' && process.versions && process.versions.node")
        if (hasNode as? String == null) {
            return
        }
        try {
            val fs = js("require('fs')")
            fs.appendFileSync(logPath, message + "\n")
        } catch (_: dynamic) {
        }
    }

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
