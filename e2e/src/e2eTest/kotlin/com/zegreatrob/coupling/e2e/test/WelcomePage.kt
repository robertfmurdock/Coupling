package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.wrapper.wdio.WebdriverBrowser

object WelcomePage : StyleSyntax {
    override val styles = loadStyles("Welcome")
    val enterButton by getting()

    private val loginChooserStyles = loadStyles("LoginChooser")
    val loginButton by loginChooserStyles.getting()

    suspend fun goTo() {
        WebdriverBrowser.setLocation("welcome")
        element().waitToExist()
    }
}
