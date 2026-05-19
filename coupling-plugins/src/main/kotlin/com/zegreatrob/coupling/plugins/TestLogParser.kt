package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

internal object TestLogParser {
    private val mapper = ObjectMapper()

    data class ParsedLog(
        val logger: String,
        val message: String,
        val properties: Map<String, Any?>,
    )

    fun parse(rawMessage: String): ParsedLog {
        val normalized = correctForPrefix(rawMessage).trimEnd()
        if (normalized.isBlank()) {
            return ParsedLog("forwarded-output", "", emptyMap())
        }

        val parsed = runCatching { mapper.readTree(normalized) }.getOrNull()
        val loggerName = parsed?.get("name")?.textValue() ?: "forwarded-output"
        val parsedMessage = parsed?.get("message")
        val message = parsedMessage?.jsonString() ?: normalized
        val properties = parsed?.get("properties").propertiesValue().orEmpty()

        return ParsedLog(loggerName, message, properties)
    }

    private fun correctForPrefix(message: String): String {
        val infoPrefix = "[info]"
        return if (message.startsWith(infoPrefix)) {
            message.substring(infoPrefix.lastIndex + 2)
        } else {
            message
        }
    }

    private fun JsonNode?.jsonString(): String? = when {
        this == null -> null
        this.isTextual -> this.textValue()
        this.isNumber -> this.numberValue().toString()
        this.isBoolean -> this.booleanValue().toString()
        this.isNull -> null
        this.isArray || this.isObject -> this.toString()
        else -> this.toString()
    }

    private fun JsonNode?.propertiesValue(): Map<String, Any?>? {
        if (this?.isObject != true) {
            return null
        }
        val values = mutableMapOf<String, Any?>()
        val names = fieldNames()
        while (names.hasNext()) {
            val key = names.next()
            val value = get(key)
            values[key] = when {
                value.isTextual -> value.textValue()
                value.isNumber -> value.numberValue()
                value.isBoolean -> value.booleanValue()
                value.isNull -> null
                else -> value.toString()
            }
        }
        return values
    }
}
