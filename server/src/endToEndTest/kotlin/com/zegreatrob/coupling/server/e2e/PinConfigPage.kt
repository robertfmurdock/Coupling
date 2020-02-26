package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object PinConfigPage : ProtractorSyntax {

    private val pinConfigStyles = loadStyles("pin/PinConfig")
    private val pinConfigPage = pinConfigStyles.element()
    val pinBag by pinConfigStyles.getting()

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

    suspend fun waitForLoad() {
        browser.wait({ pinConfigPage.isPresent() }, 2000, "PinConfigPage.waitForLoad").await()
    }
}

object PinListPage : ProtractorSyntax {

    val pinListStyles = loadStyles("pin/PinList")

    val page = pinListStyles.element()

    suspend fun waitForLoad() {
        browser.wait({ page.isPresent() }, 2000, "PinListPage.waitForLoad").await()
    }

}