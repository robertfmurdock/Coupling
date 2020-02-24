package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax

object RetiredPlayerConfig : ProtractorSyntax {

    val playerNameTextField = PlayerConfig.playerNameTextField

    suspend fun goTo(tribeId: TribeId, id: String?) {
        setLocation("/${tribeId.value}/retired-player/${id}")
        waitForPage()
    }

    suspend fun waitForPage() = PlayerConfig.waitForPage()

}