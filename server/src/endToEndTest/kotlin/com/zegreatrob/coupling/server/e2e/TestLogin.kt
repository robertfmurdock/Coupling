package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.server.e2e.external.webdriverio.browser
import kotlinx.coroutines.await

object TestLogin : BrowserSyntax {
    suspend fun login(userEmail: String) {

        for (attempt in 1..3) {
            try {
                tryLogin(userEmail)
                break
            } catch (throwable: Throwable) {
                println("Failed login attempt $attempt")
                if (attempt == 3) throw throwable
                browserGoTo("/")
            }
        }
        clearLogs()
    }

    private suspend fun clearLogs() {
        browser.getLogs("browser").await()
    }

    private suspend fun tryLogin(userEmail: String) {
        browserGoTo("${Config.publicUrl}/test-login?username=${userEmail}&password=pw")
        TribeListPage.waitForPage()
    }
}