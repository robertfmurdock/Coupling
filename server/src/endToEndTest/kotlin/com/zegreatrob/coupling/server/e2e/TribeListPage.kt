package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object TribeListPage : ProtractorSyntax {
    val tribeListStyles = loadStyles("tribe/TribeList")
    val tribeCardStyles = loadStyles("tribe/TribeCard")

    val newTribeButton = element(By.className(tribeListStyles["newTribeButton"]))
    val tribeCardElements = all(By.className(tribeCardStyles.className))
    val tribeCardHeaderLocator = By.className(tribeCardStyles["header"]);

    fun tribeCardElement(tribeId: TribeId)= element(By.css(".${tribeCardStyles.className}[data-tribe-id=\"${tribeId.value}\"]"))

    suspend fun goTo() {
        setLocation("/tribes/")
        waitForPage()
    }

    suspend fun waitForPage() {
        browser.wait({ newTribeButton.isPresent() }, 2000).await()
    }
}