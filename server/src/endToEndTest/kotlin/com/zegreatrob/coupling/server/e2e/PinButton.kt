package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.all

object PinButton : ProtractorSyntax {
    private val pinButtonStyles = loadStyles("pin/PinButton")
    val pinButtonLocator = pinButtonStyles.locator()
    val pinElements = all(pinButtonLocator)
}