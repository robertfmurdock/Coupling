package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.coupling.model.tribe.TribeId

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