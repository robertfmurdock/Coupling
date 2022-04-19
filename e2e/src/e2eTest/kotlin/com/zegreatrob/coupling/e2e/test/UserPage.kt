package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.wrapper.wdio.WebdriverBrowser

object UserPage : StyleSyntax {
    override val styles = loadStyles("user/UserConfig")

    suspend fun goTo() {
        WebdriverBrowser.setLocation("/user")
        element().waitToExist()
    }
}
