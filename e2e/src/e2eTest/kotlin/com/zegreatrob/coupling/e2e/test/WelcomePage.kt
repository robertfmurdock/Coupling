package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.external.setupBrowser
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await

private val testingBrowser = setupBrowser(browser)

object WelcomePage : BrowserSyntax {
    val enterButton get() = WebdriverElement(finder = { testingBrowser.findByText("Come on in!").await() })
    val loginButton get() = WebdriverElement(finder = { testingBrowser.findByText("Login").await() })
    suspend fun goTo() {
        WebdriverBrowser.setLocation("welcome")
        WebdriverBrowser.waitUntil(
            { enterButton.isDisplayed() },
            timeoutMessage = "Never found enter button."
        )
    }
}
