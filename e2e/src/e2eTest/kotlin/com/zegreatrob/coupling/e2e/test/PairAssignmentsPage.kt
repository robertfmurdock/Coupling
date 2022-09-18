package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PairAssignmentsPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/PairAssignments")
    val newPairsButton by getting()
    suspend fun goTo(id: PartyId) {
        WebdriverBrowser.setLocation("/${id.value}/pairAssignments/current/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }
}

object ConfigHeader : BrowserSyntax {
    suspend fun getViewHistoryButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("History!"))
    suspend fun getStatisticsButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Statistics!"))
    suspend fun getRetiredPlayersButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Retirees!"))
}

object CurrentPairAssignmentsPanel : StyleSyntax {
    override val styles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    val saveButton by getting()

    suspend fun waitForSaveButtonToNotBeDisplayed() {
        element().waitToExist()
        WebdriverBrowser.waitUntil(
            {

                try {
                    val b = !saveButton.isPresent()
                    b
                } catch (e: Error) {
                    console.log("error $e")
                    throw e
                }
            },
            2000,
            "CurrentPairAssignmentsPanel.waitForSaveButtonToNotBeDisplayed"
        )
        element().waitToExist()
    }
}

object AssignedPair : BrowserSyntax {
    val assignedPairElements = WebdriverElementArray("[data-assigned-pair]")
    val assignedPairCallSigns = WebdriverElementArray("[data-call-sign]")
}
