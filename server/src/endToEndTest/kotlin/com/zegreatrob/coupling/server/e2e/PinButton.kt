package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.all

object PinButton : StyleSyntax {
    override val styles = loadStyles("pin/PinButton")
    val pinButtonLocator = styles.locator()
    val pinElements = all(pinButtonLocator)
}