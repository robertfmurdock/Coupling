package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverElement

object PartyCard : BrowserSyntax {
    val element = WebdriverElement("[data-party-id]")
}
