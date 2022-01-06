package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.coupling.e2e.test.external.webdriverio.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.coroutines.await

object TestLogin : BrowserSyntax {
    suspend fun login(userEmail: String) {

        for (attempt in 1..3) {
            try {
                tryLogin(userEmail)
                break
            } catch (throwable: Throwable) {
                println("Failed login attempt $attempt. ${throwable.message}")
                if (attempt == 3) throw throwable
                WebdriverBrowser.setUrl("")
            }
        }

        WebdriverBrowser.setUrl("")
        TribeListPage.waitForPage()
        clearLogs()
    }

    private suspend fun clearLogs() {
        WebdriverBrowser.getLogs()
    }

    private suspend fun tryLogin(userEmail: String) {
        WebdriverBrowser.setUrl("test-login?username=${userEmail}&password=pw")
        WebdriverBrowser.waitUntil({
            try {
                "OK" == WebdriverBrowser.element("html").getText().await().trim()
            } catch (oops: Throwable) {
                false
            }
        }, 2000, "waiting for login")
    }
}