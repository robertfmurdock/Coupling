package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

object RetiredPlayersPage : StyleSyntax {
    override val styles = loadStyles("player/RetiredPlayers")

    suspend fun goTo(tribeId: PartyId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/players/retired")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }
}