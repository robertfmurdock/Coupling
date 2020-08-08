package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.*

object PinConfigPage : StyleSyntax {
    override val styles = loadStyles("pin/PinConfig")

    private suspend fun pinBag() = getting("pinBag")

    suspend fun getNameTextField() = WebdriverBrowser.element(By.id("pin-name"))
    suspend fun getIconTextField() = WebdriverBrowser.element(By.id("pin-icon"))

    suspend fun pinBagPinNames(): List<String> {
        pinBag().waitToBePresent()
        return pinBag().all(By.className("pin-name"))
            .mapSuspend { it.text() }
            .toList()
    }

    suspend fun TribeId.goToNew() {
        setLocation("/$value/pin/new")
        waitForLoad()
    }

    suspend fun goTo(tribeId: TribeId, pinId: String?) {
        setLocation("/${tribeId.value}/pin/$pinId")
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