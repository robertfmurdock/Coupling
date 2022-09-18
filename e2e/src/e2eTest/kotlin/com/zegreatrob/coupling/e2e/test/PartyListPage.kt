package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.PartyCard.element
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PartyListPage : StyleSyntax {
    override val styles = loadStyles("party/PartyList")

    suspend fun getNewPartyButton() = TestingLibraryBrowser.queryByRole("button", RoleOptions("Form a new party!"))

    val partyCardElements get() = element.all()

    fun partyCardElement(partyId: PartyId) = WebdriverElement(
        "[data-party-id=\"${partyId.value}\"]"
    )

    suspend fun goTo() {
        WebdriverBrowser.setLocation("/parties/")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ element().isPresent() }, 2000, "PartyListPage.waitForPage")
    }
}
