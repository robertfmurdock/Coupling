package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object PinConfigPage : StyleSyntax {
    override val styles = loadStyles("pin/PinConfig")

    private val pinBag by getting()

    private val pinConfigEditorStyles = loadStyles("pin/PinConfigEditor")

    val saveButton by pinConfigEditorStyles.getting()
    val deleteButton by pinConfigEditorStyles.getting()

    val nameTextField = element(By.id("pin-name"))
    val iconTextField = element(By.id("pin-icon"))

    suspend fun pinBagPinNames(): List<String> {
        pinBag.waitToBePresent()
        return pinBag.all(By.className("pin-name"))
            .map { it.getText() }
            .await()
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
        browser.wait({ element.isPresent() }, waitToBePresentDuration, "PinConfigPage.waitForLoad").await()
    }
}

object PinListPage : StyleSyntax {

    override val styles = loadStyles("pin/PinList")

    suspend fun waitForLoad() {
        browser.wait({ element.isPresent() }, waitToBePresentDuration, "PinListPage.waitForLoad").await()
    }

}