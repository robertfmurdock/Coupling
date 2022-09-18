package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PrepareToSpinPage : BrowserSyntax {

    private suspend fun getPrompt() = TestingLibraryBrowser.findByText("Please select players to spin.")
    suspend fun getSpinButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Spin!"))
    suspend fun getSelectAllButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("All in!"))
    suspend fun getSelectNoneButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("All out!"))

    private val selectedPins = WebdriverElement("[data-selected-pins]")

    val selectedPinElements get() = selectedPins.all(PinButton.pinButtonLocator)

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/prepare/")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ getPrompt().isDisplayed() }, timeoutMessage = "never arrived at prepare to spin")
    }
}
