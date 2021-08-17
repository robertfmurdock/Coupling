package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.wrapper.wdio.WebdriverBrowser

object AboutPage : StyleSyntax {
    override val styles = loadStyles("About")
    suspend fun goTo() {
        WebdriverBrowser.setLocation("about")
        element().waitToExist()
    }
}
