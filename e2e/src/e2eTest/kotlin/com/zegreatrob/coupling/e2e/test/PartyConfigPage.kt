package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.testing.library.ByRole
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PartyConfigPage : StyleSyntax, ByRole by TestingLibraryBrowser {
    override val styles = loadStyles("party/PartyConfig")

    suspend fun getPartyNameInput() = getByRole("combobox", RoleOptions(name = "Name"))
    suspend fun getPartyEmailInput() = getByRole("combobox", RoleOptions(name = "Email"))
    suspend fun getPartyIdInput() = getByRole("combobox", RoleOptions(name = "Unique Id"))
    suspend fun getCallSignCheckbox() = getByRole("checkbox", RoleOptions(name = "Enable Call Signs"))
    suspend fun getBadgeCheckbox() = getByRole("checkbox", RoleOptions(name = "Enable Badges"))
    suspend fun getDefaultBadgeNameInput() = getByRole("combobox", RoleOptions(name = "Default Badge Name"))
    suspend fun getAltBadgeNameInput() = getByRole("combobox", RoleOptions(name = "Alt Badge Name"))
    suspend fun getDifferentBadgesOption() = getByRole("option", RoleOptions(name = "Prefer Different Badges (Beta)"))
    suspend fun getCheckedOption() = WebdriverElement("#pairing-rule option:checked")

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
