package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

class WelcomeE2ETest {
    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToGoogleLogin() = e2eSetup(WelcomePage) {
        goTo()
        enterButton.performClick()
    } exercise {
        googleButton.performClick()
    } verifyAnd {
        waitToArriveAt("https://accounts.google.com")
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
        microsoftButton.performClick()
    } verify {
        waitToArriveAt("https://login.microsoftonline.com")
    }
}
