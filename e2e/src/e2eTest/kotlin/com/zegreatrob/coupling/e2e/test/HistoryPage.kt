package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElementArray

object HistoryPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/History")

    private val historyView by getting()

    val pairAssignments = WebdriverElementArray(By.className(styles["pairAssignments"]))
    val deleteButtons = WebdriverElementArray(By.className(styles["deleteButton"]))

    suspend fun goTo(tribeId: PartyId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        historyView.waitToExist()
    }
}
