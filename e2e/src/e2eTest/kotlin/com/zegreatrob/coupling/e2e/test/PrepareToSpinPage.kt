package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.external.setupBrowser
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await

private val testingBrowser = setupBrowser(browser)

object PrepareToSpinPage : BrowserSyntax {

    private val prompt
        get() = WebdriverElement(finder = {
            testingBrowser.findByText("Please select players to spin.").await()
        })
    val spinButton get() = WebdriverElement(finder = { testingBrowser.findByText("Spin!").await() })
    val selectAllButton get() = WebdriverElement(finder = { testingBrowser.findByText("All in!").await() })
    val selectNoneButton get() = WebdriverElement(finder = { testingBrowser.findByText("All out!").await() })

    private val selectedPins = WebdriverElement("[data-selected-pins]")

    val selectedPinElements get() = selectedPins.all(PinButton.pinButtonLocator)

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/prepare/")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ prompt.isDisplayed() }, timeoutMessage = "never arrived at prepare to spin")
    }
}
