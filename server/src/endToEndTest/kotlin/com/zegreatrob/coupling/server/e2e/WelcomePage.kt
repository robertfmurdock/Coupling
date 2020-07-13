package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.element
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object WelcomePage : StyleSyntax {
    override val styles = loadStyles("Welcome")
    val enterButton = element(By.className("enter-button"))
    val googleButton = element(By.className("google-login"))
    val microsoftButton = element(By.className("ms-login"))

    suspend fun goTo() {
        setLocation("/welcome")
        element.waitToBePresent()
    }

}