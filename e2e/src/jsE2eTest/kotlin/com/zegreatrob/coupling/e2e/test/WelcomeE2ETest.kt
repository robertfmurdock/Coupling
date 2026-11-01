package com.zegreatrob.coupling.e2e.test

import kotlin.test.Test

class WelcomeE2ETest {
    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToAuth0() = e2eSetup(WelcomePage) {
        goTo()
        getEnterButton().click()
    } exercise {
        getLoginButton().click()
    } verifyAnd {
        waitToArriveAtUrl("https://zegreatrob.us.auth0.com", timeout = 10000)
    } teardown {
        purgeBrowserLogsBecauseGoogleIsCreatingWarning()
    }

    private suspend fun purgeBrowserLogsBecauseGoogleIsCreatingWarning() = browserLogs().forwardLogs()
}
