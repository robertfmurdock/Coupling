package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.all
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object HistoryPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/History")

    private val historyView by getting()

    val pairAssignments = all(By.className(styles["pairAssignments"]))
    val deleteButtons = all(By.className(styles["deleteButton"]))

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        historyView.waitToBePresent()
    }

}