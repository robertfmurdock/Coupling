package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object PinConfigPage : ProtractorSyntax {

    private val pinConfigStyles = loadStyles("pin/PinConfig")
    private val pinConfigEditorStyles = loadStyles("pin/PinConfigEditor")

    private val pinConfigPage = elementFor(pinConfigStyles)

    val nameTextField = element(By.id("pin-name"))
    val iconTextField = element(By.id("pin-icon"))

    val saveButton = element(By.className(pinConfigEditorStyles["saveButton"]))
    val deleteButton = element(By.className(pinConfigEditorStyles["deleteButton"]))

    val pinBag = element(
        By.className(pinConfigStyles["pinBag"])
    )

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
        browser.wait({ pinConfigPage.isPresent() }, 2000).await()
    }
}

object PinListPage : ProtractorSyntax {

    val pinListStyles = loadStyles("pin/PinList")

    val page = elementFor(pinListStyles)

    suspend fun waitForLoad() {
        browser.wait({ page.isPresent() }, 2000).await()
    }

}