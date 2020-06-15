package com.zegreatrob.coupling.logging

import com.soywiz.klock.DateTime
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import mu.Formatter
import mu.KotlinLoggingLevel
import mu.Marker

class JsonFormatter : Formatter {

    @OptIn(UnstableDefault::class)
    override fun formatMessage(level: KotlinLoggingLevel, loggerName: String, msg: () -> Any?): Any? {
        val (message, properties) = extractProperties(msg)
        return Json.stringify(
            Message.serializer(),
            Message(
                level = level.name,
                name = loggerName,
                message = message,
                properties = properties,
                timestamp = DateTime.now().logFormat()
            )
        )
    }

    @OptIn(UnstableDefault::class)
    override fun formatMessage(
        level: KotlinLoggingLevel,
        loggerName: String,
        t: Throwable?,
        msg: () -> Any?
    ): Any? {
        val (message, properties) = extractProperties(msg)
        return Json.stringify(
            Message.serializer(),
            Message(
                level = level.name,
                name = loggerName,
                message = message,
                properties = properties,
                timestamp = DateTime.now().logFormat(),
                stackTrace = t.throwableToString()
            )
        )
    }

    @OptIn(UnstableDefault::class)
    override fun formatMessage(
        level: KotlinLoggingLevel,
        loggerName: String,
        marker: Marker?,
        msg: () -> Any?
    ): Any? {
        val (message, properties) = extractProperties(msg)
        return Json.stringify(
            Message.serializer(),
            Message(
                level = level.name,
                name = loggerName,
                message = message,
                properties = properties,
                timestamp = DateTime.now().logFormat(),
                marker = marker?.getName()
            )
        )
    }

    @OptIn(UnstableDefault::class)
    override fun formatMessage(
        level: KotlinLoggingLevel,
        loggerName: String,
        marker: Marker?,
        t: Throwable?,
        msg: () -> Any?
    ): Any? {
        val (message, properties) = extractProperties(msg)
        return Json.stringify(
            Message.serializer(),
            Message(
                level = level.name,
                name = loggerName,
                message = message,
                properties = properties,
                timestamp = DateTime.now().logFormat(),
                marker = marker?.getName(),
                stackTrace = t.throwableToString()
            )
        )
    }

    private fun extractProperties(msg: () -> Any?): Pair<String?, Map<String, String>?> {
        val result = msg()
        return if (result is Map<*, *>) {
            val map = result.unsafeCast<Map<String, String>>()
            map["message"] to map.filterKeys { it != "message" }
        } else
            result.toString() to null
    }

    private fun Throwable?.throwableToString(): List<String> {
        if (this == null) {
            return emptyList()
        }
        var msg = emptyList<String>()
        var current = this
        while (current != null && current.cause != current) {
            msg += ", Caused by: '${current.message}'"
            current = current.cause
        }
        return msg
    }

}