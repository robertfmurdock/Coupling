package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.tribeId
import com.zegreatrob.coupling.repository.player.PlayerSave

interface SdkPlayerSave : PlayerSave, GqlSyntax {
    override suspend fun save(tribeIdPlayer: TribeIdPlayer) {
        doQuery(Mutations.savePlayer, tribeIdPlayer.input())
    }
    private fun TribeIdPlayer.input() = SavePlayerInput(
        tribeId = tribeId,
        playerId = element.id,
        name = element.name,
        email = element.email,
        badge = "${element.badge}",
        callSignAdjective = element.callSignAdjective,
        callSignNoun = element.callSignNoun,
        imageURL = element.imageURL,
    )
}
