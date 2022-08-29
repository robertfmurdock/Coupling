package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement

object PartyConfigPage : StyleSyntax {
    override val styles = loadStyles("party/PartyConfig")

    fun getPartyNameInput() = WebdriverElement(By.id("party-name"))
    fun getPartyEmailInput() = WebdriverElement(By.id("party-email"))
    fun getPartyIdInput() = WebdriverElement(By.id("party-id"))
    fun getCallSignCheckbox() = WebdriverElement(By.id("call-sign-checkbox"))
    fun getBadgeCheckbox() = WebdriverElement(By.id("badge-checkbox"))
    fun getDefaultBadgeNameInput() = WebdriverElement(By.id("default-badge-name"))
    fun getAltBadgeNameInput() = WebdriverElement(By.id("alt-badge-name"))
    fun getDifferentBadgesOption() = WebdriverElement("#pairing-rule option[label=\"Prefer Different Badges (Beta)\"]")
    fun getCheckedOption() = WebdriverElement("#pairing-rule option:checked")

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/edit/")
        waitForPage()
    }

    suspend fun goToNew() {
        WebdriverBrowser.setLocation("/new-party/")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }
}
