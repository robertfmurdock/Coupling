package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.external.setupBrowser
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await

val testingBrowser = setupBrowser(browser)

object WelcomePage : BrowserSyntax {
    val enterButton get() = testingBrowser.findByText("Come on in!")

    private val loginChooserStyles = loadStyles("LoginChooser")
    val loginButton by loginChooserStyles.getting()

    suspend fun goTo() {
        WebdriverBrowser.setLocation("welcome")
        WebdriverBrowser.waitUntil(
            { enterButton.await().isDisplayed().await() },
            timeoutMessage = "Never found enter button."
        )
    }
}
