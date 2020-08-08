package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser

object HistoryPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/History")

    private val historyView by getting()

    suspend fun getPairAssignments() = WebdriverBrowser.all(By.className(styles["pairAssignments"]))
    suspend fun getDeleteButtons() = WebdriverBrowser.all(By.className(styles["deleteButton"]))

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        historyView.waitToBePresent()
    }

}