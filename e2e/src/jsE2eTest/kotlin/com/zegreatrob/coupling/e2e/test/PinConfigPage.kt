package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.e2e.test.webdriverio.WAIT_TO_BE_PRESENT_DURATION
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.testing.library.ByRole
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PinConfigPage : ByRole by TestingLibraryBrowser {

    suspend fun getNameTextField() = getByRole("combobox", RoleOptions(name = "Name"))
    suspend fun getIconTextField() = getByRole("combobox", RoleOptions(name = "Icon"))

    suspend fun pinBagPinNames() = pinBagButtons().map { it.text() }

    suspend fun pinBagButtons(): List<WebdriverElement> = WebdriverElementArray("[data-pin-button]").map { it.parentElement() }

    suspend fun PartyId.goToNew() {
        WebdriverBrowser.setLocation("/$value/pin/new")
        waitForLoad()
    }

    suspend fun goTo(partyId: PartyId, pinId: String?) {
        WebdriverBrowser.setLocation("/${partyId.value}/pin/$pinId")
        waitForLoad()
    }

    private suspend fun waitForLoad() {
        WebdriverBrowser.waitUntil({
            TestingLibraryBrowser.queryByText("Pin Configuration")
                .isPresent()
        }, WAIT_TO_BE_PRESENT_DURATION, "PinConfigPage.waitForLoad")
    }
}

object PinListPage : BrowserSyntax {
    suspend fun waitForLoad() {
        WebdriverBrowser.waitUntil(
            condition = { TestingLibraryBrowser.getByText("These are your pins.").isPresent() },
            timeout = WAIT_TO_BE_PRESENT_DURATION,
            timeoutMessage = "PinListPage.waitForLoad",
        )
    }
}
