package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class WelcomeE2ETest {

    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToGoogleLogin() = testAsync {
        setupAsync(WelcomePage) {
            goTo()
            enterButton.performClick()
        } exerciseAsync {
            googleButton.performClick()
        } verifyAsync {
            waitToArriveAt("https://accounts.google.com")
        }
    }

    @Test
    fun whenTheEnterButtonIsPressedWillRedirectToMicrosoftLogin() = testAsync {
        setupAsync(WelcomePage) {
            goTo()
            enterButton.performClick()
        } exerciseAsync {
            microsoftButton.performClick()
        } verifyAsync {
            waitToArriveAt("https://login.microsoftonline.com")
        }
    }
}

