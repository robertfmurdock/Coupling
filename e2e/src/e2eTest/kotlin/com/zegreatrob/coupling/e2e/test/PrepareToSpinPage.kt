package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

object PrepareToSpinPage : StyleSyntax {
    override val styles: SimpleStyle = loadStyles("PrepareSpin")

    val spinButton by getting()
    val selectAllButton by getting()
    val selectNoneButton by getting()
    private val selectedPins by getting()

    val selectedPinElements get() = selectedPins.all(PinButton.pinButtonLocator)

    suspend fun goTo(tribeId: PartyId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/prepare/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }
}
