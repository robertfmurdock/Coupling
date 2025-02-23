package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object ContributionListPage : BrowserSyntax {

    private suspend fun header() = TestingLibraryBrowser.getByText("Contributions")
    suspend fun lastWeekContributionHeader() = TestingLibraryBrowser.getByText(
        "Contributions for the last Week:",
    )

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil(
            { header().isDisplayed() },
            timeoutMessage = "never arrived at contribution overview",
        )
    }

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/contributions/list")
        waitForPage()
    }
}
