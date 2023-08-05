package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverElementArray

object PinButton : BrowserSyntax {
    const val PIN_BUTTON_LOCATOR = "[data-pin-button]"
    val pinElements = WebdriverElementArray(PIN_BUTTON_LOCATOR)
}
