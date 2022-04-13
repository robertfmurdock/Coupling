package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElementArray

object PairAssignmentsPage : StyleSyntax {
    override val styles = loadStyles("pairassignments/PairAssignments")
    val newPairsButton by getting()
    suspend fun goTo(id: PartyId) {
        WebdriverBrowser.setLocation("/${id.value}/pairAssignments/current/")
        waitForPage()
    }
    suspend fun waitForPage() {
        element().waitToExist()
    }
}

object ConfigHeader : StyleSyntax {
    override val styles = loadStyles("ConfigHeader")
    val viewHistoryButton by getting()
    val statisticsButton by getting()
    val retiredPlayersButton by getting()
}

object CurrentPairAssignmentsPanel : StyleSyntax {
    override val styles = loadStyles("pairassignments/CurrentPairAssignmentsPanel")
    val saveButton by getting()

    suspend fun waitForSaveButtonToNotBeDisplayed() {
        element().waitToExist()
        WebdriverBrowser.waitUntil(
            {

                try {
                    val b = !saveButton.isPresent()
                    b
                } catch (e: Error) {
                    console.log("error $e")
                    throw e
                }
            },
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
