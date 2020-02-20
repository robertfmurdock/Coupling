package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.element
import kotlinx.coroutines.await

object TribeListPage : ProtractorSyntax {
    val tribeListStyles = loadStyles("tribe/TribeList")
    val newTribeButton = element(
        By.className(tribeListStyles["newTribeButton"])
    )

    suspend fun wait() {
        browser.wait({ newTribeButton.isPresent() }, 2000).await()
    }
}