package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.element
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object WelcomePage : ProtractorSyntax {

    private val welcomeStyles = loadStyles("Welcome")

    val enterButton = element(By.className("enter-button"))
    val googleButton = element(By.className("google-login"))
    val microsoftButton = element(By.className("ms-login"))

    private val welcomePage = welcomeStyles.element()

    suspend fun goTo() {
        setLocation("/welcome")
        welcomePage.waitToBePresent()
    }

}