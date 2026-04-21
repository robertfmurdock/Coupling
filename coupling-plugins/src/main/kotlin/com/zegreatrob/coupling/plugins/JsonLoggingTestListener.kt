package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.api.tasks.testing.TestOutputListener
import org.gradle.api.tasks.testing.TestResult
import java.time.Instant

class JsonLoggingTestListener(
    private val taskName: String,
    private val testRunIdentifier: String,
    private val logFilePath: String,
) :
    TestListener,
    TestOutputListener {

    companion object {
        val mapper = ObjectMapper()
    }

    override fun beforeTest(testDescriptor: TestDescriptor) {
        appendEvent(
            "TestStart",
            testDescriptor = testDescriptor,
            properties = emptyMap(),
        )
    }

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        val durationMs = (result.endTime - result.startTime)
        val failureSummary = result.exceptions
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n") { "${it::class.simpleName}: ${it.message}" }
        appendEvent(
            "TestEnd",
            testDescriptor = testDescriptor,
            status = "${result.resultType}",
            durationMs = durationMs,
            message = failureSummary,
            properties = mapOf("failure_count" to result.exceptions.size),
        )
    }

    override fun beforeSuite(suite: TestDescriptor?) {
    }

    override fun afterSuite(suite: TestDescriptor?, result: TestResult?) {
    }

    override fun onOutput(testDescriptor: TestDescriptor?, outputEvent: TestOutputEvent?) {
        if (outputEvent == null) {
            return
        }
        val normalized = correctForPrefix(outputEvent.message).trimEnd()
        if (normalized.isBlank()) {
            return
        }
        val parsed = runCatching { mapper.readTree(normalized) }.getOrNull()
        val loggerName = parsed?.get("name")?.textValue() ?: "forwarded-output"
        val parsedMessage = parsed?.get("message")
        val message = parsedMessage?.jsonString() ?: normalized
        val properties = parsed?.get("properties").propertiesValue().orEmpty()

        appendEvent(
            type = "Log",
            testDescriptor = testDescriptor,
            message = message,
            logger = loggerName,
            properties = properties,
        )
    }

    private fun correctForPrefix(message: String): String {
        val infoPrefix = "[info]"
        return if (message.startsWith(infoPrefix)) {
            message.substring(infoPrefix.lastIndex + 2)
        } else {
            message
        }
    }

    private fun JsonNode?.jsonString(): String? {
        return when {
            this == null -> null
            this.isTextual -> this.textValue()
            this.isNumber -> this.numberValue().toString()
            this.isBoolean -> this.booleanValue().toString()
            this.isNull -> null
            this.isArray || this.isObject -> this.toString()
            else -> this.toString()
        }
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

    private fun appendEvent(
        type: String,
        testDescriptor: TestDescriptor?,
        status: String? = null,
        durationMs: Long? = null,
        message: String? = null,
        logger: String = "test-events",
        properties: Map<String, Any?> = emptyMap(),
    ) {
        val event = linkedMapOf<String, Any?>(
            "type" to type,
            "task" to taskName,
            "suite" to testDescriptor?.parent?.name,
            "test" to testDescriptor?.name,
            "run_id" to testRunIdentifier,
            "platform" to inferPlatform(taskName),
            "timestamp" to Instant.now().toString(),
            "logger" to logger,
        )
        status?.let { event["status"] = it }
        durationMs?.let { event["duration_ms"] = it }
        message?.let { event["message"] = it }
        if (properties.isNotEmpty()) {
            event["properties"] = properties
        }
        TestLoggingFileAppender.appendEvent(logFilePath, event)
    }

    private fun inferPlatform(task: String): String {
        return when {
            task.contains("jvm", ignoreCase = true) -> "jvm"
            task.contains("e2e", ignoreCase = true) -> "e2e"
            else -> "js"
        }
    }
}
