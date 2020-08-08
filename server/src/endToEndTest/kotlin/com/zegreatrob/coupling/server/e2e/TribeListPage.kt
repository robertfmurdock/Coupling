package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.*

object TribeListPage : StyleSyntax {
    override val styles = loadStyles("tribe/TribeList")
    private val tribeCardStyles = loadStyles("tribe/TribeCard")

    suspend fun getNewTribeButton() = getting("newTribeButton")
    suspend fun getTribeCardElements() = all(By.className(tribeCardStyles.className))
    val tribeCardHeaderLocator = By.className(tribeCardStyles["header"])

    suspend fun tribeCardElement(tribeId: TribeId) =
        element(".${tribeCardStyles.className}[data-tribe-id=\"${tribeId.value}\"]")

    suspend fun goTo() {
        setLocation("/tribes/")
        waitForPage()
    }

    suspend fun waitForPage() {
        waitUntil({ element().isPresent() }, 2000, "TribeListPage.waitForPage")
    }
}
