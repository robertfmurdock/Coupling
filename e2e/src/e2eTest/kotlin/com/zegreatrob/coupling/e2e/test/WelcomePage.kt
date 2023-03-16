package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object WelcomePage : BrowserSyntax {
    suspend fun getEnterButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Come on in!"))
    suspend fun getLoginButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Login"))
    suspend fun goTo() {
        WebdriverBrowser.setLocation("welcome")
        WebdriverBrowser.waitUntil(
            { getEnterButton().isDisplayed() },
            timeoutMessage = "Never found enter button.",
        )
    }
}
