package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object PrepareToSpinPage : ProtractorSyntax {

    val prepareSpinStyles = loadStyles("PrepareSpin")

    val pageElement = prepareSpinStyles.element()
    val spinButton by prepareSpinStyles.getting()

    val selectedPins by prepareSpinStyles.getting()
    val selectedPinElements = selectedPins.all(PinButton.pinButtonLocator)

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/prepare/")
        waitForPage()
    }

    suspend fun waitForPage() {
        pageElement.waitToBePresent()
    }
}