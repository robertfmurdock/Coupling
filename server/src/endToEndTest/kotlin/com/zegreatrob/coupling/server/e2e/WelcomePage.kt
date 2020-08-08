package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.waitToBePresent

object WelcomePage : StyleSyntax {
    override val styles = loadStyles("Welcome")
    val enterButton by getting()

    private val loginChooserStyles = loadStyles("LoginChooser")
    val googleLoginButton by loginChooserStyles.getting()
    val microsoftLoginButton by loginChooserStyles.getting()

    suspend fun goTo() {
        setLocation("/welcome")
        element().waitToBePresent()
    }

}