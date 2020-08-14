package com.zegreatrob.coupling.e2e

import com.zegreatrob.testmints.async.invoke
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class WelcomeE2ETest {
    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToGoogleLogin() = e2eSetup(WelcomePage) {
        goTo()
        enterButton.click()
    } exercise {
        googleLoginButton.click()
    } verifyAnd {
        waitToArriveAtUrl("https://accounts.google.com")
    } teardown {
        purgeBrowserLogsBecauseGoogleIsCreatingWarning()
    }

    private suspend fun purgeBrowserLogsBecauseGoogleIsCreatingWarning() = WebdriverBrowser.getLogs()

    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToMicrosoftLogin() = e2eSetup(WelcomePage) {
        goTo()
        enterButton.click()
    } exercise {
        microsoftLoginButton.click()
    } verify {
        waitToArriveAtUrl("https://login.microsoftonline.com")
    }
}
