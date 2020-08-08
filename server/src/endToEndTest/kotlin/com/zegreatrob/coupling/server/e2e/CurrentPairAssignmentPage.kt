package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.*
import kotlin.js.Promise

object CurrentPairAssignmentPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    suspend fun saveButton() = getting("saveButton")

    private val pairAssignmentsStyles = loadStyles("pairassignments/PairAssignments")
    suspend fun viewHistoryButton() = pairAssignmentsStyles.element("viewHistoryButton")
    suspend fun newPairsButton() = pairAssignmentsStyles.element("newPairsButton")
    suspend fun statisticsButton() = pairAssignmentsStyles.element("statisticsButton")
    suspend fun retiredPlayersButton() = pairAssignmentsStyles.element("retiredPlayersButton")

    private val assignedPairStyles = loadStyles("pairassignments/AssignedPair")
    suspend fun getAssignedPairElements() = all(By.className(assignedPairStyles.className))
    suspend fun getAssignedPairCallSigns() = all(By.className(assignedPairStyles["callSign"]))

    suspend fun goTo(id: TribeId) {
        setLocation("/${id.value}/pairAssignments/current/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToBePresent()
    }

    suspend fun waitForSaveButtonToNotBeDisplayed() {
        waitForPage()
        waitUntil(
            { saveButton().isNotPresent() },
            2000,
            "CurrentPairAssignmentPage.waitForSaveButtonToNotBeDisplayed"
        )
        waitForPage()
    }

    private suspend fun Promise<Element>.isNotPresent() = !isPresent()

}
