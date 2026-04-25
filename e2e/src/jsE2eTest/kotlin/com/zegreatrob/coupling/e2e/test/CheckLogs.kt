package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.js.Json
import kotlin.js.json

suspend fun checkLogs() {
    WebdriverBrowser.getLogs()
        .let { browserLog ->
            browserLog.forwardLogs()
            errorsWarnings(browserLog)
                .assertIsEqualTo(emptyList(), JSON.stringify(errorsWarnings(browserLog)))
        }
}

fun List<Json>.forwardLogs() = forEach {
    try {
        val forwarded = parseForForwarding(it)
        val forwardedText = JSON.stringify(forwarded)
        console.log(forwardedText)
        val normalized = normalizeCommandLog(
            logger = forwarded["name"]?.toString() ?: "browser",
            message = forwarded["message"]?.toString() ?: forwardedText,
            properties = forwarded,
        )
        appendCanonicalToTestLog(
            message = normalized.message,
            logger = normalized.logger,
            properties = normalized.properties,
        )
    } catch (_: Throwable) {
        val fallback = it["message"].toString()
        console.log(fallback)
        appendCanonicalToTestLog(
            message = fallback,
            logger = "browser",
            properties = json("source" to "browser", "raw" to fallback),
        )
    }
}

private fun parseForForwarding(it: Json): Json {
    val message = it["message"].toString()
    val embeddedJson = parseEmbeddedJsonLog(message)
        ?: return json("source" to "browser", "message" to message)
    return json("source" to "browser").add(embeddedJson)
}

private fun errorsWarnings(browserLog: List<Json>) = browserLog.filter {
    when (it["level"]) {
        "ERROR" -> true
        "WARN" -> true
        else -> false
    }
}.map { it["message"] }

private fun parseEmbeddedJsonLog(message: String): Json? {
    Regex("\"\\{.*\\}\"$").find(message)?.value?.let { quotedJson ->
        runCatching { JSON.parse<String>(quotedJson) }
            .mapCatching { JSON.parse<Json>(it) }
            .getOrNull()
            ?.let { return it }
    }

    val firstBrace = message.indexOf('{')
    val lastBrace = message.lastIndexOf('}')
    if (firstBrace < 0 || lastBrace <= firstBrace) {
        return null
    }
    val jsonText = message.substring(firstBrace, lastBrace + 1)
    return runCatching { JSON.parse<Json>(jsonText) }.getOrNull()
}

private data class NormalizedLog(
    val logger: String,
    val message: String,
    val properties: Json,
)

private fun normalizeCommandLog(logger: String, message: String, properties: Json): NormalizedLog {
    val propertyAction = properties["command_action"]?.toString() ?: properties["action"]?.toString()
    val propertyPhase = properties["command_phase"]?.toString() ?: properties["type"]?.toString()?.lowercase()
    val propertyTraceId = properties["command_trace_id"]?.toString() ?: properties["traceId"]?.toString()
    val propertyDurationMs = numericDurationMs(properties["command_duration_ms"]?.toString())
        ?: numericDurationMs(properties["duration"]?.toString())

    val messageAction = commandValue(message, "command_action") ?: commandValue(message, "action")
    val messagePhase = commandValue(message, "command_phase") ?: commandValue(message, "type")?.lowercase()
    val messageTraceId = commandValue(message, "command_trace_id") ?: commandValue(message, "traceId")
    val messageDurationMs = numericDurationMs(commandValue(message, "command_duration_ms"))
        ?: numericDurationMs(commandValue(message, "duration"))

    val action = propertyAction ?: messageAction
    val phase = propertyPhase ?: messagePhase
    if (action.isNullOrBlank() || phase.isNullOrBlank()) {
        if (logger == "ActionLogger") {
            return NormalizedLog(
                logger = "forwarded-output",
                message = message,
                properties = properties.add(json("forwarded_logger" to "ActionLogger")),
            )
        }
        return NormalizedLog(logger = logger, message = message, properties = properties)
    }

    val normalizedPhase = when (phase.lowercase()) {
        "start" -> "start"
        "end" -> "end"
        else -> phase.lowercase()
    }

    val normalizedProperties = properties
        .add(
            json(
                "command" to true,
                "command_action" to action,
                "command_phase" to normalizedPhase,
            ),
        )
        .let {
            val withTrace = (propertyTraceId ?: messageTraceId)?.let { traceId ->
                it.add(json("command_trace_id" to traceId))
            } ?: it
            (propertyDurationMs ?: messageDurationMs)?.let { duration ->
                withTrace.add(json("command_duration_ms" to duration))
            } ?: withTrace
        }

    return NormalizedLog(
        logger = "command",
        message = "$action:$normalizedPhase",
        properties = normalizedProperties,
    )
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

internal fun appendCanonicalToTestLog(message: String, logger: String, properties: Json) {
    val logPath = testLogPath() ?: return
    if (!isNodeRuntime()) {
        return
    }
    try {
        val fs = js("require('fs')")
        val testIdentity = currentTestIdentity()
        val enrichedProperties = properties
            .let {
                val withSuite = testIdentity.suite?.let { suite -> it.add(json("test_suite" to suite)) } ?: it
                val withTest = testIdentity.test?.let { test -> withSuite.add(json("test_name" to test)) } ?: withSuite
                testIdentity.testId?.let { testId -> withTest.add(json("test_id" to testId)) } ?: withTest
            }
        val event = json(
            "type" to "Log",
            "platform" to "e2e",
            "run_id" to testRunId(),
            "task" to testTaskPath(),
            "suite" to testIdentity.suite,
            "test" to testIdentity.test,
            "logger" to logger,
            "message" to message,
            "timestamp" to nowIsoTimestamp(),
            "properties" to enrichedProperties,
        )
        fs.appendFileSync(logPath, JSON.stringify(event) + "\n")
    } catch (_: dynamic) {
    }
}

internal fun appendTestLifecycleToTestLog(type: String, status: String? = null, durationMs: Double? = null) {
    val logPath = testLogPath() ?: return
    if (!isNodeRuntime()) {
        return
    }
    try {
        val fs = js("require('fs')")
        val testIdentity = currentTestIdentity()
        val event = json(
            "type" to type,
            "platform" to "e2e",
            "run_id" to testRunId(),
            "task" to testTaskPath(),
            "suite" to testIdentity.suite,
            "test" to testIdentity.test,
            "timestamp" to nowIsoTimestamp(),
        ).let {
            val withStatus = status?.let { testStatus -> it.add(json("status" to testStatus)) } ?: it
            durationMs?.let { duration -> withStatus.add(json("duration_ms" to duration)) } ?: withStatus
        }
        fs.appendFileSync(logPath, JSON.stringify(event) + "\n")
    } catch (_: dynamic) {
    }
}

private fun testLogPath(): String? {
    val envVar = js("typeof process !== 'undefined' && process.env ? process.env.COUPLING_TEST_LOG_PATH : null")
    return envVar as? String
}

private fun testRunId(): String {
    val envVar = js("typeof process !== 'undefined' && process.env ? process.env.COUPLING_TEST_RUN_ID : null")
    return envVar as? String ?: "unknown-run"
}

private fun testTaskPath(): String {
    val envVar = js("typeof process !== 'undefined' && process.env ? process.env.COUPLING_TEST_TASK : null")
    return envVar as? String ?: ":e2e:e2eRun"
}

private fun nowIsoTimestamp(): String = js("new Date().toISOString()") as String

private fun isNodeRuntime(): Boolean {
    val hasNode = js("typeof process !== 'undefined' && process.versions && process.versions.node")
    return hasNode as? String != null
}

private data class TestIdentity(
    val suite: String?,
    val test: String?,
    val testId: String?,
)

private fun currentTestIdentity(): TestIdentity {
    val suiteFromEnv = js("typeof process !== 'undefined' && process.env ? process.env.COUPLING_CURRENT_TEST_SUITE : null") as? String
    val testFromEnv = js("typeof process !== 'undefined' && process.env ? process.env.COUPLING_CURRENT_TEST_NAME : null") as? String
    val testIdFromEnv = js("typeof process !== 'undefined' && process.env ? process.env.COUPLING_CURRENT_TEST_ID : null") as? String
    if (suiteFromEnv != null || testFromEnv != null || testIdFromEnv != null) {
        return TestIdentity(
            suite = suiteFromEnv,
            test = testFromEnv,
            testId = testIdFromEnv,
        )
    }
    val context = js("globalThis.__couplingCurrentTest")
    return TestIdentity(
        suite = context?.suite as? String,
        test = context?.test as? String,
        testId = context?.testId as? String,
    )
}
