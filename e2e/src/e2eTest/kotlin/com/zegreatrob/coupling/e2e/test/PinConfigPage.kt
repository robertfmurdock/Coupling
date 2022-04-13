package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement

object PinConfigPage : StyleSyntax {
    override val styles = loadStyles("pin/PinConfig")

    private val pinBag by getting()

    fun getNameTextField() = WebdriverElement(By.id("pin-name"))
    fun getIconTextField() = WebdriverElement(By.id("pin-icon"))

    suspend fun pinBagPinNames(): List<String> {
        pinBag.waitToExist()
        return pinBag.all(By.className("pin-name"))
            .map { it.text() }
    }

    suspend fun PartyId.goToNew() {
        WebdriverBrowser.setLocation("/$value/pin/new")
        waitForLoad()
    }

    suspend fun goTo(tribeId: PartyId, pinId: String?) {
        WebdriverBrowser.setLocation("/${tribeId.value}/pin/$pinId")
        waitForLoad()
    }

    private suspend fun waitForLoad() {
        WebdriverBrowser.waitUntil({ element().isPresent() }, waitToBePresentDuration, "PinConfigPage.waitForLoad")
    }
}

object PinListPage : StyleSyntax {

    override val styles = loadStyles("pin/PinList")

    suspend fun waitForLoad() {
        WebdriverBrowser.waitUntil({ element().isPresent() }, waitToBePresentDuration, "PinListPage.waitForLoad")
    }

}