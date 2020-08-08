package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.all
import com.zegreatrob.coupling.server.e2e.external.webdriverio.waitToBePresent

object PrepareToSpinPage : StyleSyntax {
    override val styles: SimpleStyle = loadStyles("PrepareSpin")

    suspend fun getSpinButton() = getting("spinButton")
    suspend fun getSelectAllButton() = getting("selectAllButton")
    suspend fun getSelectNoneButton() = getting("selectNoneButton")
    suspend private fun getSelectedPins() = getting("selectedPins")

    suspend fun getSelectedPinElements() = getSelectedPins().all(PinButton.pinButtonLocator)

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/prepare/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToBePresent()
    }
}
