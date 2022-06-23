package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.PartyCard.element
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement

object PartyListPage : StyleSyntax {
    override val styles = loadStyles("party/PartyList")

    val newPartyButton by getting()

    val partyCardElements get() = element.all()

    fun partyCardElement(tribeId: PartyId) = WebdriverElement(
        "[data-tribe-id=\"${tribeId.value}\"]"
    )

    suspend fun goTo() {
        WebdriverBrowser.setLocation("/tribes/")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ element().isPresent() }, 2000, "PartyListPage.waitForPage")
    }
}
