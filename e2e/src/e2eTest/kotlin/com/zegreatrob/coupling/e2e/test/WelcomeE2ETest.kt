package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class WelcomeE2ETest {
    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToAuth0() = e2eSetup(WelcomePage) {
        goTo()
        enterButton.click()
    } exercise {
        loginButton.click()
    } verifyAnd {
        waitToArriveAtUrl("https://zegreatrob.us.auth0.com")
    } teardown {
        purgeBrowserLogsBecauseGoogleIsCreatingWarning()
    }

    private suspend fun purgeBrowserLogsBecauseGoogleIsCreatingWarning() = WebdriverBrowser.getLogs().forwardLogs()
}
