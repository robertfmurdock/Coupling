package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.wrapper.wdio.WebdriverElementArray

object PinButton : StyleSyntax {
    override val styles = loadStyles("pin/PinButton")
    val pinButtonLocator = styles.locator
    val pinElements = WebdriverElementArray(pinButtonLocator)
}
