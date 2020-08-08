package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser

object PinButton : StyleSyntax {
    override val styles = loadStyles("pin/PinButton")
    val pinButtonLocator = styles.locator()
    suspend fun getPinElements() = WebdriverBrowser.all(pinButtonLocator)
}
