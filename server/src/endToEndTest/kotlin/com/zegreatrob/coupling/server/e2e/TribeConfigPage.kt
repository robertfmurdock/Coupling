package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.element
import com.zegreatrob.coupling.server.e2e.external.webdriverio.waitToBePresent

object TribeConfigPage : StyleSyntax {
    override val styles = loadStyles("tribe/TribeConfig")

    suspend fun getTribeNameInput() = element(By.id("tribe-name"))
    suspend fun getTribeEmailInput() = element(By.id("tribe-email"))
    suspend fun getTribeIdInput() = element(By.id("tribe-id"))
    suspend fun getCallSignCheckbox() = element(By.id("call-sign-checkbox"))
    suspend fun getBadgeCheckbox() = element(By.id("badge-checkbox"))
    suspend fun getDefaultBadgeNameInput() = element(By.id("default-badge-name"))
    suspend fun getAltBadgeNameInput() = element(By.id("alt-badge-name"))
    suspend fun getDifferentBadgesOption() = element("#pairing-rule option[label=\"Prefer Different Badges (Beta)\"]")
    suspend fun getCheckedOption() = element("#pairing-rule option:checked")

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/edit/")
        waitForPage()
    }

    suspend fun goToNew() {
        setLocation("/new-tribe/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToBePresent()
    }
}