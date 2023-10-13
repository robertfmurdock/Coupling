package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser
import kotlin.test.Test

class UserConfigE2ETest {

    @Test
    fun loadingWillShowBasicInformationAboutTheUser() = e2eSetup(object {
        val page = UserPage
    }) exercise {
        page.goTo()
    } verify {
        val emailInput = TestingLibraryBrowser.findByLabelText("User Email")
        WebdriverBrowser.waitUntil({
            emailInput.isDisplayed()
        })
        emailInput.attribute("value")
            .assertIsEqualTo(PRIMARY_AUTHORIZED_USERNAME)
    }
}
