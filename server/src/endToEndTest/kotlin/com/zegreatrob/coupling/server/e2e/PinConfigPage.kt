package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.element
import kotlinx.coroutines.await

object PinConfigPage : ProtractorSyntax {

    val pinConfigStyles = loadStyles("pin/PinConfig")
    val pinConfigEditorStyles = loadStyles("pin/PinConfigEditor")

    val pinConfigPage = element(
        By.className(pinConfigStyles.className)
    )
    val nameTextField = element(
        By.id("pin-name")
    )

    val saveButton = element(
        By.className(pinConfigEditorStyles["saveButton"])
    )
    val pinBag = element(
        By.className(pinConfigStyles["pinBag"])
    )

    suspend fun goToNewPinConfig(tribeId: TribeId) {
        setLocation("/${tribeId.value}/pin/new")
        wait()
    }

    suspend fun goToPinConfig(tribeId: TribeId, pinId: String?) {
        setLocation("/${tribeId.value}/pin/$pinId")
        wait()
    }

    suspend fun wait() {
        browser.wait({ pinConfigPage.isPresent() }, 2000).await()
    }
}