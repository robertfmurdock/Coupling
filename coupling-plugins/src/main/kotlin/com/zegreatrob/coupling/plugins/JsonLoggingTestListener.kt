package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.api.tasks.testing.TestOutputListener
import org.gradle.api.tasks.testing.TestResult
import java.time.Instant
import java.util.UUID

class JsonLoggingTestListener(
    private val taskName: String,
    private val testRunIdentifier: String,
    private val logFilePath: String,
) :
    TestListener,
    TestOutputListener {

    companion object {
        val mapper = ObjectMapper()
        private val canonicalTestMintsPhases = listOf(
            "setup-start",
            "setup-finish",
            "exercise-start",
            "exercise-finish",
            "verify-start",
            "verify-finish",
            "test-start",
            "test-finish",
        )
    }

    private val testIdentityByDescriptor = mutableMapOf<Int, TestIdentity>()
    private val testOccurrenceByIdentityKey = mutableMapOf<String, Int>()

    private data class TestIdentity(
        val suite: String,
        val test: String,
        val testId: String,
    )

    override fun beforeTest(testDescriptor: TestDescriptor) {
        val testIdentity = identityForStart(testDescriptor)
        appendEvent(
            "TestStart",
            testIdentity = testIdentity,
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
            testIdentity = identityForEnd(testDescriptor),
            status = "${result.resultType}",
            durationMs = durationMs,
            message = failureSummary,
            properties = mapOf("failure_count" to result.exceptions.size),
        )
        synchronized(this) { testIdentityByDescriptor.remove(descriptorKey(testDescriptor)) }
    }

    override fun beforeSuite(suite: TestDescriptor?) = Unit

    override fun afterSuite(suite: TestDescriptor?, result: TestResult?) = Unit

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
        val commandNormalized = normalizeCommandLog(loggerName, message, properties)
        val testIdentity = identityForLog(testDescriptor)
        val attributedProperties = addTestAttribution(
            properties = commandNormalized.properties,
            logger = commandNormalized.logger,
            testIdentity = testIdentity,
        )
        val normalizedLog = normalizeTestmintsLog(
            loggerName = commandNormalized.logger,
            message = commandNormalized.message,
            properties = attributedProperties,
        )

        appendEvent(
            type = "Log",
            testIdentity = testIdentity,
            message = normalizedLog.message,
            logger = normalizedLog.logger,
            properties = normalizedLog.properties,
        )
    }

    private data class NormalizedLogEvent(
        val logger: String,
        val message: String,
        val properties: Map<String, Any?>,
    )

    private fun normalizeTestmintsLog(
        loggerName: String,
        message: String,
        properties: Map<String, Any?>,
    ): NormalizedLogEvent {
        val phase = testmintsPhase(message)
        val isTestmints = loggerName.equals("testmints", ignoreCase = true) ||
            phase != null ||
            message.contains("[testmints]") ||
            message.contains("testmints -")
        if (!isTestmints) {
            return NormalizedLogEvent(
                logger = loggerName,
                message = message,
                properties = properties,
            )
        }

        val step = testmintsValue(message, "step")
        val state = testmintsValue(message, "state")
        val name = testmintsName(message)
        val enriched = properties.toMutableMap().apply {
            put("testmints", true)
            phase?.let { put("testmints_phase", it) }
            step?.let { put("testmints_step", it) }
            state?.let { put("testmints_state", it) }
            name?.let { put("testmints_name", it) }
        }

        return NormalizedLogEvent(
            logger = "testmints",
            message = phase ?: message,
            properties = enriched,
        )
    }

    private fun normalizeCommandLog(
        loggerName: String,
        message: String,
        properties: Map<String, Any?>,
    ): NormalizedLogEvent {
        val propertyAction = properties["command_action"]?.toString()
            ?: properties["action"]?.toString()
        val propertyPhase = properties["command_phase"]?.toString()
            ?: properties["type"]?.toString()?.lowercase()
        val propertyTraceId = properties["command_trace_id"]?.toString()
            ?: properties["traceId"]?.toString()
        val propertyDurationMs = numericDurationMs(properties["command_duration_ms"]?.toString())
            ?: numericDurationMs(properties["duration"]?.toString())

        val messageAction = commandValue(message, "command_action")
            ?: commandValue(message, "action")
        val messagePhase = commandValue(message, "command_phase")
            ?: commandValue(message, "type")?.lowercase()
        val messageTraceId = commandValue(message, "command_trace_id")
            ?: commandValue(message, "traceId")
        val messageDurationMs = numericDurationMs(commandValue(message, "command_duration_ms"))
            ?: numericDurationMs(commandValue(message, "duration"))

        val action = propertyAction ?: messageAction
        val phase = propertyPhase ?: messagePhase
        if (action.isNullOrBlank() || phase.isNullOrBlank()) {
            return NormalizedLogEvent(
                logger = loggerName,
                message = message,
                properties = properties,
            )
        }

        val normalizedPhase = when (phase.lowercase()) {
            "start" -> "start"
            "end" -> "end"
            else -> phase.lowercase()
        }
        val enriched = properties.toMutableMap().apply {
            put("command", true)
            put("command_action", action)
            put("command_phase", normalizedPhase)
            (propertyTraceId ?: messageTraceId)?.let { put("command_trace_id", it) }
            (propertyDurationMs ?: messageDurationMs)?.let { put("command_duration_ms", it) }
        }

        return NormalizedLogEvent(
            logger = "command",
            message = "$action:$normalizedPhase",
            properties = enriched,
        )
    }

    private fun addTestAttribution(
        properties: Map<String, Any?>,
        logger: String,
        testIdentity: TestIdentity?,
    ): Map<String, Any?> {
        if (testIdentity == null) {
            return properties
        }
        val enriched = properties.toMutableMap().apply {
            put("test_suite", testIdentity.suite)
            put("test_name", testIdentity.test)
            put("test_id", testIdentity.testId)
            if (logger == "command") {
                put("test_task", taskName)
                put("test_platform", inferPlatform(taskName))
            }
        }
        return enriched
    }

    private fun numericDurationMs(raw: String?): Double? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return when {
            raw.endsWith("ms") -> raw.removeSuffix("ms").toDoubleOrNull()
            raw.endsWith("s") -> raw.removeSuffix("s").toDoubleOrNull()?.times(1000.0)
            else -> raw.toDoubleOrNull()
        }
    }

    private fun commandValue(message: String, key: String): String? {
        val regex = Regex("""\b${Regex.escape(key)}=([^,}\s]+)""")
        return regex.find(message)?.groupValues?.get(1)?.trim()
    }

    private fun testmintsPhase(message: String): String? {
        canonicalTestMintsPhases.firstOrNull { message.contains(it) }?.let { return it }

        return when {
            message.contains("setupStart") -> "setup-start"
            message.contains("setupFinish") -> "setup-finish"
            message.contains("exerciseStart") -> "exercise-start"
            message.contains("exerciseFinish") -> "exercise-finish"
            message.contains("verifyStart") -> "verify-start"
            message.contains("verifyFinish") -> "verify-finish"
            message.contains("testStart") -> "test-start"
            message.contains("testFinish") -> "test-finish"
            else -> null
        }
    }

    private fun testmintsValue(message: String, key: String): String? {
        val regex = Regex("""\\b${Regex.escape(key)}=([^,}\\s]+)""")
        return regex.find(message)?.groupValues?.get(1)
    }

    private fun testmintsName(message: String): String? {
        val regex = Regex("""\\bname=([^,}]+)""")
        return regex.find(message)?.groupValues?.get(1)?.trim()
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
        testIdentity: TestIdentity?,
        status: String? = null,
        durationMs: Long? = null,
        message: String? = null,
        logger: String = "test-events",
        properties: Map<String, Any?> = emptyMap(),
    ) {
        val event = linkedMapOf<String, Any?>(
            "type" to type,
            "task" to taskName,
            "suite" to testIdentity?.suite,
            "test" to testIdentity?.test,
            "test_id" to testIdentity?.testId,
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

    private fun identityForLog(testDescriptor: TestDescriptor?): TestIdentity? = synchronized(this) {
        testDescriptor ?: return null
        testIdentityByDescriptor[descriptorKey(testDescriptor)]
            ?: computeIdentity(testDescriptor, assignOccurrence = false)
    }

    private fun identityForStart(testDescriptor: TestDescriptor): TestIdentity = synchronized(this) {
        computeIdentity(testDescriptor, assignOccurrence = true).also {
            testIdentityByDescriptor[descriptorKey(testDescriptor)] = it
        }
    }

    private fun identityForEnd(testDescriptor: TestDescriptor): TestIdentity = synchronized(this) {
        testIdentityByDescriptor[descriptorKey(testDescriptor)]
            ?: computeIdentity(testDescriptor, assignOccurrence = true).also {
                testIdentityByDescriptor[descriptorKey(testDescriptor)] = it
            }
    }

    private fun descriptorKey(testDescriptor: TestDescriptor): Int = System.identityHashCode(testDescriptor)

    private fun computeIdentity(testDescriptor: TestDescriptor, assignOccurrence: Boolean): TestIdentity {
        val suite = suiteName(testDescriptor)
        val test = testName(testDescriptor)
        val identityKey = "$taskName||$suite||$test"
        val occurrence = if (assignOccurrence) {
            val next = (testOccurrenceByIdentityKey[identityKey] ?: 0) + 1
            testOccurrenceByIdentityKey[identityKey] = next
            next
        } else {
            testOccurrenceByIdentityKey[identityKey] ?: 1
        }
        val opaque = UUID.nameUUIDFromBytes(
            "$testRunIdentifier||$taskName||$suite||$test||$occurrence".toByteArray()
        ).toString()
        return TestIdentity(
            suite = suite,
            test = test,
            testId = opaque,
        )
    }

    private fun suiteName(testDescriptor: TestDescriptor): String =
        testDescriptor.className?.takeIf { it.isNotBlank() }
            ?: testDescriptor.parent?.name?.takeIf { it.isNotBlank() }
            ?: "unknown-suite"

    private fun testName(testDescriptor: TestDescriptor): String =
        testDescriptor.name.takeIf { it.isNotBlank() } ?: "unknown-test"

    private fun inferPlatform(task: String): String {
        return when {
            task.contains("jvm", ignoreCase = true) -> "jvm"
            task.contains("e2e", ignoreCase = true) -> "e2e"
            else -> "js"
        }
    }
}
