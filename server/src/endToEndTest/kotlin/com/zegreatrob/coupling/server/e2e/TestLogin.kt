package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser

object TestLogin : BrowserSyntax {
    suspend fun login(userEmail: String) {

        for (attempt in 1..3) {
            try {
                tryLogin(userEmail)
                break
            } catch (throwable: Throwable) {
                println("Failed login attempt $attempt")
                if (attempt == 3) throw throwable
                WebdriverBrowser.setUrl("/")
            }
        }
        clearLogs()
    }

    private suspend fun clearLogs() {
        WebdriverBrowser.getLogs()
    }

    private suspend fun tryLogin(userEmail: String) {
        WebdriverBrowser.setUrl("${Config.publicUrl}/test-login?username=${userEmail}&password=pw")
        TribeListPage.waitForPage()
    }
}