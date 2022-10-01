package com.zegreatrob.coupling.e2e.test

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
        WebdriverBrowser.waitUntil({
            TestingLibraryBrowser.findByText("User Email: $primaryAuthorizedUsername").isDisplayed()
        })
    }
}
