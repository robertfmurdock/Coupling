package com.zegreatrob.coupling.plugins

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
) : TestListener,
    TestOutputListener {

    private val identityTracker = TestIdentityTracker(taskName, testRunIdentifier)

    override fun beforeTest(testDescriptor: TestDescriptor) {
        val testIdentity = identityTracker.identityForStart(testDescriptor)
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
            testIdentity = identityTracker.identityForEnd(testDescriptor),
            status = "${result.resultType}",
            durationMs = durationMs,
            message = failureSummary,
            properties = mapOf("failure_count" to result.exceptions.size),
        )
        identityTracker.clearIdentity(testDescriptor)
    }

    override fun beforeSuite(suite: TestDescriptor?) = Unit

    override fun afterSuite(suite: TestDescriptor?, result: TestResult?) = Unit

    override fun onOutput(testDescriptor: TestDescriptor?, outputEvent: TestOutputEvent?) {
        if (outputEvent == null) {
            return
        }
        val parsed = TestLogParser.parse(outputEvent.message)
        if (parsed.message.isBlank()) {
            return
        }
        val commandNormalized = CommandLogNormalizer.normalize(parsed.logger, parsed.message, parsed.properties)
        val testIdentity = identityTracker.identityForLog(testDescriptor)
        val attributedProperties = addTestAttribution(
            properties = commandNormalized.properties,
            logger = commandNormalized.logger,
            testIdentity = testIdentity,
        )
        val normalizedLog = TestmintsLogNormalizer.normalize(
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

    private fun addTestAttribution(
        properties: Map<String, Any?>,
        logger: String,
        testIdentity: TestIdentityTracker.TestIdentity?,
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

    private fun appendEvent(
        type: String,
        testIdentity: TestIdentityTracker.TestIdentity?,
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

    private fun inferPlatform(task: String): String = when {
        task.contains("jvm", ignoreCase = true) -> "jvm"
        task.contains("e2e", ignoreCase = true) -> "e2e"
        else -> "js"
    }
}
