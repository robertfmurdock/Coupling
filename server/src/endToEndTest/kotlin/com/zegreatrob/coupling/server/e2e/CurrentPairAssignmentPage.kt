package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object CurrentPairAssignmentPage : ProtractorSyntax {

    val currentPairAssignmentsPanelStyles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    val pageElement = elementFor(currentPairAssignmentsPanelStyles)
    val saveButton = element(By.className(currentPairAssignmentsPanelStyles["saveButton"]))

    val pairAssignmentsStyles = loadStyles("pairassignments/PairAssignments")
    val viewHistoryButton = pairAssignmentsStyles.elementWithClass("viewHistoryButton")
    val newPairsButton = pairAssignmentsStyles.elementWithClass("newPairsButton")
    val statisticsButton = pairAssignmentsStyles.elementWithClass("statisticsButton")
    val retiredPlayersButton = pairAssignmentsStyles.elementWithClass("retiredPlayersButton")

    val assignedPairStyles = loadStyles("pairassignments/AssignedPair")
    val assignedPairElements = all(By.className(assignedPairStyles.className))
    val assignedPairCallSigns = all(By.className(assignedPairStyles["callSign"]))

    suspend fun goTo(id: TribeId) {
        setLocation("/${id.value}/pairAssignments/current/")
        waitForPage()
    }

    suspend fun waitForPage() {
        pageElement.waitToBePresent()
    }

    suspend fun waitForSaveButtonToNotBeDisplayed() {
        waitForPage()
        browser.wait(
            { saveButton.isNotPresent() },
            2000,
            "CurrentPairAssignmentPage.waitForSaveButtonToNotBeDisplayed"
        ).await()
        waitForPage()
    }

    private fun ElementSelector.isNotPresent() = isPresent().then({ !it }, { false })
}