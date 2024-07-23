package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object HistoryPage : BrowserSyntax {
    private suspend fun getHistoryView() = TestingLibraryBrowser.findAllByText("History!")
    val pairAssignments get() = WebdriverElementArray("[data-pair-assignments-id]")

    suspend fun getDeleteButtons() = TestingLibraryBrowser.getAllByRole("button", RoleOptions(name = "DELETE"))

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ getHistoryView().count() == 2 }, timeoutMessage = "never arrived at history")
    }
}

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
