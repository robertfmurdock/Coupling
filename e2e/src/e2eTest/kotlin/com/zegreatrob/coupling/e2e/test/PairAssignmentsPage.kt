package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.external.setupBrowser
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await

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

private val testingBrowser = setupBrowser(browser)

object ConfigHeader : BrowserSyntax {
    val viewHistoryButton get() = WebdriverElement(finder = { testingBrowser.findByText("History!").await() })
    val statisticsButton get() = WebdriverElement(finder = { testingBrowser.findByText("Statistics!").await() })
    val retiredPlayersButton get() = WebdriverElement(finder = { testingBrowser.findByText("Retirees!").await() })
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
