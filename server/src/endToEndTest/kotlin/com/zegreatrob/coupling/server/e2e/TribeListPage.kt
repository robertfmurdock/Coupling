package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object TribeListPage : ProtractorSyntax {
    val tribeListStyles = loadStyles("tribe/TribeList")
    val tribeCardStyles = loadStyles("tribe/TribeCard")

    val newTribeButton = element(By.className(tribeListStyles["newTribeButton"]))
    val tribeCardElements = all(By.className(tribeCardStyles.className))

    suspend fun goTo() {
        setLocation("/tribes/")
        waitForPage()
    }

    suspend fun waitForPage() {
        browser.wait({ newTribeButton.isPresent() }, 2000).await()
    }
}