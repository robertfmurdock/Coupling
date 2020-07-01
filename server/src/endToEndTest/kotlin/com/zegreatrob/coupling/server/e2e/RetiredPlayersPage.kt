package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object RetiredPlayersPage : ProtractorSyntax {

    private val retiredPlayersPage = loadStyles("player/RetiredPlayers")
    private val pageElement = retiredPlayersPage.element()

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/players/retired")
        waitForPage()
    }

    suspend fun waitForPage() {
        pageElement.waitToBePresent()
    }
}