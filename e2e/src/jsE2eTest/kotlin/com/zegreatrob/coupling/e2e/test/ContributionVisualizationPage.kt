package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object ContributionVisualizationPage : BrowserSyntax {

    private suspend fun visualizationStyleLabel() = TestingLibraryBrowser.getByText("Visualization Style")
    suspend fun lastWeekContributionHeader() = TestingLibraryBrowser.getByText(
        "Contributions for the last Week:",
    )

    suspend fun waitForPage() = WebdriverBrowser.waitUntil(
        { visualizationStyleLabel().isDisplayed() },
        timeoutMessage = "never arrived at contribution visualizations",
    )

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/contributions/visualization")
        waitForPage()
    }
}
