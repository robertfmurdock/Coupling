package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverElementArray
import com.zegreatrob.coupling.server.e2e.external.webdriverio.isPresent

object TribeListPage : StyleSyntax {
    override val styles = loadStyles("tribe/TribeList")
    private val tribeCardStyles = loadStyles("tribe/TribeCard")

    val newTribeButton by getting()

    suspend fun getTribeCardElements() = WebdriverElementArray(By.className(tribeCardStyles.className))
    val tribeCardHeaderLocator = By.className(tribeCardStyles["header"])

    suspend fun tribeCardElement(tribeId: TribeId) =
        WebdriverBrowser.element(".${tribeCardStyles.className}[data-tribe-id=\"${tribeId.value}\"]")

    suspend fun goTo() {
        setLocation("/tribes/")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ element().isPresent() }, 2000, "TribeListPage.waitForPage")
    }
}
