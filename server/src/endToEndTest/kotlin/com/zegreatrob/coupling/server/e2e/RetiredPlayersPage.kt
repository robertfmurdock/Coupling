package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser

object RetiredPlayersPage : StyleSyntax {
    override val styles = loadStyles("player/RetiredPlayers")

    suspend fun goTo(tribeId: TribeId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/players/retired")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }
}