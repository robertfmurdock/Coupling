package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.coupling.server.e2e.external.webdriverio.waitToBePresent

object CurrentPairAssignmentPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    val saveButton by getting()

    private val assignedPairStyles = loadStyles("pairassignments/AssignedPair")
    suspend fun getAssignedPairElements() = WebdriverBrowser.all(By.className(assignedPairStyles.className))
    suspend fun getAssignedPairCallSigns() = WebdriverBrowser.all(By.className(assignedPairStyles["callSign"]))

    suspend fun goTo(id: TribeId) {
        setLocation("/${id.value}/pairAssignments/current/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToBePresent()
    }

    suspend fun waitForSaveButtonToNotBeDisplayed() {
        waitForPage()
        WebdriverBrowser.waitUntil(
            { saveButton.isNotPresent() },
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
