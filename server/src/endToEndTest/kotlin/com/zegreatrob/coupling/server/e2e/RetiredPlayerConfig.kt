package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.BrowserSyntax

object RetiredPlayerConfig : BrowserSyntax {

    suspend fun getPlayerNameTextField() = PlayerConfigPage.playerNameTextField()

    suspend fun goTo(tribeId: TribeId, id: String?) {
        setLocation("/${tribeId.value}/retired-player/${id}")
        waitForPage()
    }

    suspend fun waitForPage() = PlayerConfigPage.waitForPage()

}