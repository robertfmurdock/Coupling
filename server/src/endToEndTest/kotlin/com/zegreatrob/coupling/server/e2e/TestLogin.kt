package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import kotlinx.coroutines.await

object TestLogin : ProtractorSyntax {
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
        if (browser.getCapabilities().await()["browserName"] != "firefox") {
            browser.manage().logs().get("browser").await()
        }
    }

    private suspend fun tryLogin(userEmail: String) {
        browserGoTo("${Config.publicUrl}/test-login?username=${userEmail}&password=pw")
        TribeListPage.waitForPage()
    }
}