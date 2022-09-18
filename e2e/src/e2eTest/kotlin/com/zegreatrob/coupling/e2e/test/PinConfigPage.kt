package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.ByRole
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PinConfigPage : StyleSyntax, ByRole by TestingLibraryBrowser {
    override val styles = loadStyles("pin/PinConfig")

    private val pinBag by getting()

    suspend fun getNameTextField() = getByRole("combobox", RoleOptions(name = "Name"))
    suspend fun getIconTextField() = getByRole("combobox", RoleOptions(name = "Icon"))

    suspend fun pinBagPinNames(): List<String> {
        pinBag.waitToExist()
        return pinBag.all(By.className("pin-name"))
            .map { it.text() }
    }

    suspend fun PartyId.goToNew() {
        WebdriverBrowser.setLocation("/$value/pin/new")
        waitForLoad()
    }

    suspend fun goTo(partyId: PartyId, pinId: String?) {
        WebdriverBrowser.setLocation("/${partyId.value}/pin/$pinId")
        waitForLoad()
    }

    private suspend fun waitForLoad() {
        WebdriverBrowser.waitUntil({
            TestingLibraryBrowser.queryByText("Pin Configuration")
                .isPresent()
        }, waitToBePresentDuration, "PinConfigPage.waitForLoad")
    }
}

object PinListPage : StyleSyntax {

    override val styles = loadStyles("pin/PinList")

    suspend fun waitForLoad() {
        WebdriverBrowser.waitUntil({ element().isPresent() }, waitToBePresentDuration, "PinListPage.waitForLoad")
    }
}
