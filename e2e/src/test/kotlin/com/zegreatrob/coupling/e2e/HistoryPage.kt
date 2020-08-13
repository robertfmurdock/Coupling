package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.wdio.By
import com.zegreatrob.coupling.wdio.WebdriverBrowser
import com.zegreatrob.coupling.wdio.WebdriverElementArray
import com.zegreatrob.coupling.model.tribe.TribeId

object HistoryPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/History")

    private val historyView by getting()

    val pairAssignments =
        WebdriverElementArray(By.className(styles["pairAssignments"]))
    val deleteButtons =
        WebdriverElementArray(By.className(styles["deleteButton"]))

    suspend fun goTo(tribeId: TribeId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        historyView.waitToExist()
    }

}