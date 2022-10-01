package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PartyListPage {

    suspend fun getNewPartyButton() = TestingLibraryBrowser.queryByRole("button", RoleOptions("Form a new party!"))

    val partyCardElements get() = PartyCard.element.all()

    fun partyCardElement(partyId: PartyId) = WebdriverElement(
        "[data-party-id=\"${partyId.value}\"]"
    )

    suspend fun goTo() {
        WebdriverBrowser.setLocation("/parties/")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil(
            { TestingLibraryBrowser.getByText("Party List").isDisplayed() },
            2000,
            "PartyListPage.waitForPage"
        )
    }
}
