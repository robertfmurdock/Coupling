package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.all
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object HistoryPage : ProtractorSyntax {
    val historyStyles = loadStyles("pairassignments/History")

    val historyView by historyStyles.getting()

    val pairAssignments = all(By.className(historyStyles["pairAssignments"]))
    val deleteButtons = all(By.className(historyStyles["deleteButton"]))

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        historyView.waitToBePresent()
    }

}