package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverElementArray

object CurrentPairAssignmentPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    val saveButton by getting()

    private val assignedPairStyles = loadStyles("pairassignments/AssignedPair")
    val assignedPairElements = WebdriverElementArray(By.className(assignedPairStyles.className))
    val assignedPairCallSigns = WebdriverElementArray(By.className(assignedPairStyles["callSign"]))

    suspend fun goTo(id: TribeId) {
        setLocation("/${id.value}/pairAssignments/current/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }

    suspend fun waitForSaveButtonToNotBeDisplayed() {
        waitForPage()
        WebdriverBrowser.waitUntil(
            { !saveButton.isPresent() },
            2000,
            "CurrentPairAssignmentPage.waitForSaveButtonToNotBeDisplayed"
        )
        waitForPage()
    }

}

object PairAssignments : StyleSyntax {
    override val styles = loadStyles("pairassignments/PairAssignments")
    val viewHistoryButton by getting()
    val newPairsButton by getting()
    val statisticsButton by getting()
    val retiredPlayersButton by getting()
}
