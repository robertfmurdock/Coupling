package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object CurrentPairAssignmentPage : ProtractorSyntax {

    val currentPairAssignmentsPanelStyles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    val assignedPairStyles = loadStyles("pairassignments/AssignedPair")

    val pageElement = elementFor(currentPairAssignmentsPanelStyles)
    val saveButton = element(
        By.className(
            currentPairAssignmentsPanelStyles["saveButton"]
        )
    )
    val assignedPairElements = all(
        By.className(assignedPairStyles.className)
    )

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