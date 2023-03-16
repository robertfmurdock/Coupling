package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object StatisticsPage {

    suspend fun rotationNumber() = TestingLibraryBrowser.getByText("Spins Until Full Rotation:").nextElement()

    suspend fun pairReports() = TestingLibraryBrowser.getAllByText("Stats").map { it.parentElement() }

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/statistics")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil(
            { TestingLibraryBrowser.getByText("Statistics").isDisplayed() },
            timeoutMessage = "Failed to load stats page",
        )
    }
}
