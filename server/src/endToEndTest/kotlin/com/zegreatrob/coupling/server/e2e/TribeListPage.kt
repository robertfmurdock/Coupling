package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.all
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.element
import kotlinx.coroutines.await

object TribeListPage : StyleSyntax {
    override val styles = loadStyles("tribe/TribeList")
    private val tribeCardStyles = loadStyles("tribe/TribeCard")

    val newTribeButton by getting()
    val tribeCardElements = all(By.className(tribeCardStyles.className))
    val tribeCardHeaderLocator = By.className(tribeCardStyles["header"])

    fun tribeCardElement(tribeId: TribeId) =
        element(By.css(".${tribeCardStyles.className}[data-tribe-id=\"${tribeId.value}\"]"))

    suspend fun goTo() {
        setLocation("/tribes/")
        waitForPage()
    }

    suspend fun waitForPage() {
        browser.wait({ element.isPresent() }, 2000, "TribeListPage.waitForPage").await()
    }
}
