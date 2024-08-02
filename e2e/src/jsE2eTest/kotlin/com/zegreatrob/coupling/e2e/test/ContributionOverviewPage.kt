package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object ContributionOverviewPage : BrowserSyntax {

    private suspend fun header() = TestingLibraryBrowser.getByText("Contributions")
    suspend fun setupInstructions() = TestingLibraryBrowser.getByText("Want to get started with Contributions?")
    suspend fun mostRecentHeader(recentContributions: Int) = TestingLibraryBrowser.getByText(
        "Most Recent $recentContributions Contributions:",
    )

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil(
            { header().isDisplayed() },
            timeoutMessage = "never arrived at contribution overview",
        )
    }

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/contributions")
        waitForPage()
    }
}
