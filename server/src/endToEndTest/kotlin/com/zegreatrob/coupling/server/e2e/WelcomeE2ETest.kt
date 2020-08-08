package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.performClick
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

class WelcomeE2ETest {
    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToGoogleLogin() = e2eSetup(WelcomePage) {
        goTo()
        getEnterButton().performClick()
    } exercise {
        getGoogleButton().performClick()
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
        getEnterButton().performClick()
    } exercise {
        getMicrosoftButton().performClick()
    } verify {
        waitToArriveAtUrl("https://login.microsoftonline.com")
    }
}
