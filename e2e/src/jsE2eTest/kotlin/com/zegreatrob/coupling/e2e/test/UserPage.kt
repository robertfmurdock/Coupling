package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object UserPage {
    suspend fun goTo() {
        WebdriverBrowser.setLocation("/user")
        WebdriverBrowser.waitUntil({ TestingLibraryBrowser.getByText("User Config").isDisplayed() })
    }
}
