package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import kotlin.test.Test

class WelcomeE2ETest {
    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToGoogleLogin() = e2eSetup(WelcomePage) {
        goTo()
        enterButton.performClick()
    } exercise {
        googleButton.performClick()
    } verify {
        waitToArriveAt("https://accounts.google.com")
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
