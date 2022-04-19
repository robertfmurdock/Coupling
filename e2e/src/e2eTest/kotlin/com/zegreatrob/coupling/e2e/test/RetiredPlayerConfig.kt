package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

object RetiredPlayerConfig : BrowserSyntax {

    val playerNameTextField get() = PlayerConfigPage.playerNameTextField()

    suspend fun goTo(tribeId: PartyId, id: String?) {
        WebdriverBrowser.setLocation("/${tribeId.value}/retired-player/$id")
        waitForPage()
    }

    suspend fun waitForPage() = PlayerConfigPage.waitForPage()
}
