package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement

object PartyConfigPage : StyleSyntax {
    override val styles = loadStyles("party/PartyConfig")

    fun getTribeNameInput() = WebdriverElement(By.id("tribe-name"))
    fun getTribeEmailInput() = WebdriverElement(By.id("tribe-email"))
    fun getTribeIdInput() = WebdriverElement(By.id("tribe-id"))
    fun getCallSignCheckbox() = WebdriverElement(By.id("call-sign-checkbox"))
    fun getBadgeCheckbox() = WebdriverElement(By.id("badge-checkbox"))
    fun getDefaultBadgeNameInput() = WebdriverElement(By.id("default-badge-name"))
    fun getAltBadgeNameInput() = WebdriverElement(By.id("alt-badge-name"))
    fun getDifferentBadgesOption() = WebdriverElement("#pairing-rule option[label=\"Prefer Different Badges (Beta)\"]")
    fun getCheckedOption() = WebdriverElement("#pairing-rule option:checked")

    suspend fun goTo(tribeId: PartyId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/edit/")
        waitForPage()
    }

    suspend fun goToNew() {
        WebdriverBrowser.setLocation("/new-tribe/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }
}
