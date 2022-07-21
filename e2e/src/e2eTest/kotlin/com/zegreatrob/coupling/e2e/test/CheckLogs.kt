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
        console.log(JSON.stringify(parseForForwarding(it)))
    } catch (_: Throwable) {
        console.log(it["message"])
    }
}

private fun parseForForwarding(it: Json): Json {
    val message = it["message"].toString()
    val messageJson: String = JSON.parse(message.substring(message.indexOf("\"{")))
    return json("source" to "browser").add(JSON.parse(messageJson))
}

private fun errorsWarnings(browserLog: List<Json>) = browserLog.filter {
    when (it["level"]) {
        "ERROR" -> true
        "WARN" -> true
        else -> false
    }
}.map { it["message"] }
