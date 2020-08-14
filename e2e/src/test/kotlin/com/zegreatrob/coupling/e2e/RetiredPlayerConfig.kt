package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.e2e.external.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

object RetiredPlayerConfig : BrowserSyntax {

    suspend fun getPlayerNameTextField() = PlayerConfigPage.playerNameTextField()

    suspend fun goTo(tribeId: TribeId, id: String?) {
        WebdriverBrowser.setLocation("/${tribeId.value}/retired-player/${id}")
        waitForPage()
    }

    suspend fun waitForPage() = PlayerConfigPage.waitForPage()

}