package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.element
import com.zegreatrob.coupling.server.e2e.external.webdriverio.waitToBePresent

object WelcomePage : StyleSyntax {
    override val styles = loadStyles("Welcome")
    suspend fun getEnterButton() = element(By.className("enter-button"))
    suspend fun getGoogleButton() = element(By.className("google-login"))
    suspend fun getMicrosoftButton() = element(By.className("ms-login"))

    suspend fun goTo() {
        setLocation("/welcome")
        element().waitToBePresent()
    }

}