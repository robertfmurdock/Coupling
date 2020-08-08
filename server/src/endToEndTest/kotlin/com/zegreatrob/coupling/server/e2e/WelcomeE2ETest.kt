package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

class WelcomeE2ETest {
    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToGoogleLogin() = e2eSetup(WelcomePage) {
        goTo()
        enterButton.performClick()
    } exercise {
        googleLoginButton.performClick()
    } verifyAnd {
        waitToArriveAtUrl("https://accounts.google.com")
    } teardown {
        purgeBrowserLogsBecauseGoogleIsCreatingWarning()
    }

    private suspend fun purgeBrowserLogsBecauseGoogleIsCreatingWarning() {
        getBrowserLogs()
    }

    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToMicrosoftLogin() = e2eSetup(WelcomePage) {
        goTo()
        enterButton.performClick()
    } exercise {
        microsoftLoginButton.performClick()
    } verify {
        waitToArriveAtUrl("https://login.microsoftonline.com")
    }
}
