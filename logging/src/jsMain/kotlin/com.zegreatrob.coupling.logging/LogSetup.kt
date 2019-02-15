package com.zegreatrob.coupling.logging

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializable
import mu.*

@Serializable
data class Message(
        val level: String,
        val name: String,
        val message: String?,
        val properties: Map<String, String>?,
        val timestamp: String,
        val marker: String? = null,
        val stackTrace: List<String>? = null
)

fun DateTime.logFormat() = toString(DateFormat.FORMAT1)

@Suppress("unused")
@JsName("initializeLogging")
fun initializeLogging(developmentMode: Boolean) {
    LOG_LEVEL = if (developmentMode) {
        KotlinLoggingLevel.DEBUG
    } else {
        KotlinLoggingLevel.INFO
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    mu.messageFormatter = object : MessageFormatter {

        override fun formatMessage(level: KotlinLoggingLevel, loggerName: String, msg: () -> Any?): Any? {
            val (message, properties) = extractProperties(msg)
            return kotlinx.serialization.json.Json.stringify(Message.serializer(), Message(
                    level = level.name,
                    name = loggerName,
                    message = message,
                    properties = properties,
                    timestamp = DateTime.now().logFormat()
            ))
        }

        override fun formatMessage(level: KotlinLoggingLevel, loggerName: String, t: Throwable?, msg: () -> Any?): Any? {
            val (message, properties) = extractProperties(msg)
            return kotlinx.serialization.json.Json.stringify(Message.serializer(), Message(
                    level = level.name,
                    name = loggerName,
                    message = message,
                    properties = properties,
                    timestamp = DateTime.now().logFormat(),
                    stackTrace = t.throwableToString()
            ))
        }

        override fun formatMessage(level: KotlinLoggingLevel, loggerName: String, marker: Marker?, msg: () -> Any?): Any? {
            val (message, properties) = extractProperties(msg)
            return kotlinx.serialization.json.Json.stringify(Message.serializer(), Message(
                    level = level.name,
                    name = loggerName,
                    message = message,
                    properties = properties,
                    timestamp = DateTime.now().logFormat(),
                    marker = marker?.getName()
            ))
        }

        override fun formatMessage(level: KotlinLoggingLevel, loggerName: String, marker: Marker?, t: Throwable?, msg: () -> Any?): Any? {
            val (message, properties) = extractProperties(msg)
            return kotlinx.serialization.json.Json.stringify(Message.serializer(), Message(
                    level = level.name,
                    name = loggerName,
                    message = message,
                    properties = properties,
                    timestamp = DateTime.now().logFormat(),
                    marker = marker?.getName(),
                    stackTrace = t.throwableToString()
            ))
        }

        private fun extractProperties(msg: () -> Any?): Pair<String?, Map<String, String>?> {
            val result = msg()
            return if (result is Map<*, *>) {
                null to result.unsafeCast<Map<String, String>>()
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

    }.unsafeCast<DefaultMessageFormatter>()
}