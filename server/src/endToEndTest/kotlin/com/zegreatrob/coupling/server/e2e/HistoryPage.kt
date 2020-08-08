package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.all
import com.zegreatrob.coupling.server.e2e.external.webdriverio.waitToBePresent

object HistoryPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/History")

    private suspend fun getHistoryView() = getting("historyView")

    suspend fun getPairAssignments() = all(By.className(styles["pairAssignments"]))
    suspend fun getDeleteButtons() = all(By.className(styles["deleteButton"]))

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        getHistoryView().waitToBePresent()
    }

}