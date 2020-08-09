package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser

object TribeConfigPage : StyleSyntax {
    override val styles = loadStyles("tribe/TribeConfig")

    suspend fun getTribeNameInput() = WebdriverBrowser.element(By.id("tribe-name"))
    suspend fun getTribeEmailInput() = WebdriverBrowser.element(By.id("tribe-email"))
    suspend fun getTribeIdInput() = WebdriverBrowser.element(By.id("tribe-id"))
    suspend fun getCallSignCheckbox() = WebdriverBrowser.element(By.id("call-sign-checkbox"))
    suspend fun getBadgeCheckbox() = WebdriverBrowser.element(By.id("badge-checkbox"))
    suspend fun getDefaultBadgeNameInput() = WebdriverBrowser.element(By.id("default-badge-name"))
    suspend fun getAltBadgeNameInput() = WebdriverBrowser.element(By.id("alt-badge-name"))
    suspend fun getDifferentBadgesOption() = WebdriverBrowser.element("#pairing-rule option[label=\"Prefer Different Badges (Beta)\"]")
    suspend fun getCheckedOption() = WebdriverBrowser.element("#pairing-rule option:checked")

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
