package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object CurrentPairAssignmentPage : ProtractorSyntax {

    val currentPairAssignmentsPanelStyles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    val pageElement = currentPairAssignmentsPanelStyles.element()
    val saveButton by currentPairAssignmentsPanelStyles.getting()

    val pairAssignmentsStyles = loadStyles("pairassignments/PairAssignments")
    val viewHistoryButton by pairAssignmentsStyles.getting()
    val newPairsButton by pairAssignmentsStyles.getting()
    val statisticsButton by pairAssignmentsStyles.getting()
    val retiredPlayersButton by pairAssignmentsStyles.getting()

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

