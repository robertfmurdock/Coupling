package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElementArray

object PairAssignmentsPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/PairAssignments")
    val viewHistoryButton by getting()
    val newPairsButton by getting()
    val statisticsButton by getting()
    val retiredPlayersButton by getting()

    suspend fun goTo(id: TribeId) {
        WebdriverBrowser.setLocation("/${id.value}/pairAssignments/current/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }

}

object CurrentPairAssignmentsPanel : StyleSyntax {
    override val styles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    val saveButton by getting()

    suspend fun waitForSaveButtonToNotBeDisplayed() {
        element().waitToExist()
        WebdriverBrowser.waitUntil(
            { !saveButton.isPresent() },
            2000,
            "CurrentPairAssignmentsPanel.waitForSaveButtonToNotBeDisplayed"
        )
        element().waitToExist()
    }

}

object AssignedPair : StyleSyntax {
    override val styles: SimpleStyle = loadStyles("pairassignments/AssignedPair")
    val assignedPairElements = WebdriverElementArray(By.className(styles.className))
    val assignedPairCallSigns = WebdriverElementArray(By.className(styles["callSign"]))
}
