package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.wdio.WebdriverBrowser

object WelcomePage : StyleSyntax {
    override val styles = loadStyles("Welcome")
    val enterButton by getting()

    private val loginChooserStyles = loadStyles("LoginChooser")
    val googleLoginButton by loginChooserStyles.getting()
    val microsoftLoginButton by loginChooserStyles.getting()

    suspend fun goTo() {
        WebdriverBrowser.setLocation("/welcome")
        element().waitToExist()
    }

}