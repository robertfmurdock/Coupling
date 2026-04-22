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
        appendCanonicalToTestLog(
            message = forwarded["message"]?.toString() ?: forwardedText,
            logger = forwarded["name"]?.toString() ?: "browser",
            properties = forwarded,
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
    val messageJson: String = JSON.parse(message.substringAfter("\"{"))
    return json("source" to "browser").add(JSON.parse(messageJson))
}

private fun errorsWarnings(browserLog: List<Json>) = browserLog.filter {
    when (it["level"]) {
        "ERROR" -> true
        "WARN" -> true
        else -> false
    }
}.map { it["message"] }

private fun appendCanonicalToTestLog(message: String, logger: String, properties: Json) {
    val logPath = testLogPath() ?: return
    if (!isNodeRuntime()) {
        return
    }
    try {
        val fs = js("require('fs')")
        val event = json(
            "type" to "Log",
            "platform" to "e2e",
            "run_id" to testRunId(),
            "task" to testTaskPath(),
            "logger" to logger,
            "message" to message,
            "timestamp" to nowIsoTimestamp(),
            "properties" to properties,
        )
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
