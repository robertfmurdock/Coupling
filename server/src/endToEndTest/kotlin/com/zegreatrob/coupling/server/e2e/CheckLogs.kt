package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.js.Json
import kotlin.js.json

suspend fun checkLogs() {
    WebdriverBrowser.getLogs()
        .let { browserLog ->
            browserLog.forEach {
                console.log(JSON.stringify(parseForForwarding(it)))
            }
            errorsWarnings(browserLog)
                .assertIsEqualTo(emptyList(), JSON.stringify(errorsWarnings(browserLog)))
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
