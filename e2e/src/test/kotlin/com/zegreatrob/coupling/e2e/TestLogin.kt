package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

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
        WebdriverBrowser.setUrl("/test-login?username=${userEmail}&password=pw")
        TribeListPage.waitForPage()
    }
}