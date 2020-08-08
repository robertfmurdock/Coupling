package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.browser
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.await
import kotlin.js.Json

suspend fun checkLogs() {
    browser.getLogs("browser").await().toList()
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

suspend fun getBrowserLogs() = browser.getLogs("browser").await()
