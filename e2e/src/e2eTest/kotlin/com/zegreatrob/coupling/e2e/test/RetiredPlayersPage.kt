package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object RetiredPlayersPage : BrowserSyntax {

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/players/retired")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil(
            { TestingLibraryBrowser.getByText("Retired Players").isDisplayed() }
        )
    }
}
