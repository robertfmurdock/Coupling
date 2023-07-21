package com.zegreatrob.coupling.logging

import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.Level
import io.github.oshai.kotlinlogging.Marker
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class JsonFormatter : Formatter {

    override fun formatMessage(
        level: Level,
        loggerName: String,
        marker: Marker?,
        throwable: Throwable?,
        message: () -> Any?,
    ): Any {
        val (msg, properties) = extractProperties(message)
        return Json.encodeToString(
            Message.serializer(),
            Message(
                level = level.name,
                name = loggerName,
                message = msg,
                properties = properties,
                timestamp = Clock.System.now().toString(),
                marker = marker?.getName(),
                stackTrace = throwable.throwableToString(),
            ),
        )
    }

    private fun extractProperties(msg: () -> Any?): Pair<String?, Map<String, String>?> {
        val result = msg()
        return if (result is Map<*, *>) {
            val map = result.unsafeCast<Map<String, Any>>()
            val propertyGroupName = map["message"]?.toString()
            val propertyMap = map.filterKeys { it != "message" }
                .mapValues { entry -> entry.value.toString() }
            propertyGroupName to propertyMap
        } else {
            result.toString() to null
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
