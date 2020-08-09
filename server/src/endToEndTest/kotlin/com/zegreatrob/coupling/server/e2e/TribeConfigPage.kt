package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverElement

object TribeConfigPage : StyleSyntax {
    override val styles = loadStyles("tribe/TribeConfig")

    fun getTribeNameInput() = WebdriverElement(By.id("tribe-name"))
    fun getTribeEmailInput() = WebdriverElement(By.id("tribe-email"))
    fun getTribeIdInput() = WebdriverElement(By.id("tribe-id"))
    fun getCallSignCheckbox() = WebdriverElement(By.id("call-sign-checkbox"))
    fun getBadgeCheckbox() = WebdriverElement(By.id("badge-checkbox"))
    fun getDefaultBadgeNameInput() = WebdriverElement(By.id("default-badge-name"))
    fun getAltBadgeNameInput() = WebdriverElement(By.id("alt-badge-name"))
    fun getDifferentBadgesOption() = WebdriverElement("#pairing-rule option[label=\"Prefer Different Badges (Beta)\"]")
    fun getCheckedOption() = WebdriverElement("#pairing-rule option:checked")

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
