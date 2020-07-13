package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object PrepareToSpinPage : StyleSyntax {
    override val styles: SimpleStyle = loadStyles("PrepareSpin")

    val spinButton by getting()
    val selectAllButton by getting()
    val selectNoneButton by getting()

    private val selectedPins by getting()

    val selectedPinElements = selectedPins.all(PinButton.pinButtonLocator)

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/prepare/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element.waitToBePresent()
    }
}
