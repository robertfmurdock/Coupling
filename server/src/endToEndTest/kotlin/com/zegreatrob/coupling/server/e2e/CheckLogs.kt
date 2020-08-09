package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.js.Json

suspend fun checkLogs() {
    WebdriverBrowser.getLogs()
        .let { browserLog ->
            browserLog.forEach { console.log("BROWSER_LOG", it["level"], it["message"]) }
            errorsWarnings(browserLog)
                .assertIsEqualTo(emptyList(), JSON.stringify(errorsWarnings(browserLog)))
        }
}

private fun errorsWarnings(browserLog: List<Json>) = browserLog.filter {
    when (it["level"]) {
        "ERROR" -> true
        "WARN" -> true
        else -> false
    }
}.map { it["message"] }
