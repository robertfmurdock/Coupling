package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*

object HistoryPage : ProtractorSyntax {
    val historyStyles = loadStyles("pairassignments/History")

    val historyView = element(
        By.className(historyStyles["historyView"])
    )

    val pairAssignmentDocElements = all(
        By.className(historyStyles["pairAssignments"])
    )
    val deleteButtons = all(
        By.className(historyStyles["deleteButton"])
    )

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        historyView.waitToBePresent()
    }

}