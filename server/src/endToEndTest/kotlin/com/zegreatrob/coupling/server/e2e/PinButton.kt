package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.all

object PinButton : ProtractorSyntax {
    val pinButtonStyles = loadStyles("pin/PinButton")
    val pinButtonLocator =
        By.className(pinButtonStyles.className)

    val pinElements = all(pinButtonLocator)
}