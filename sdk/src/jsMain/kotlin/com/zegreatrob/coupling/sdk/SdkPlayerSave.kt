package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.tribeId
import com.zegreatrob.coupling.repository.player.PlayerSave
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.json

interface SdkPlayerSave : PlayerSave, GqlSyntax {
    override suspend fun save(tribeIdPlayer: TribeIdPlayer) {
        performQuery(
            json(
                "query" to Mutations.savePlayer,
                "variables" to json("input" to tribeIdPlayer.savePlayerInput())
            )
        )
    }

    private fun TribeIdPlayer.savePlayerInput() = couplingJsonFormat.encodeToDynamic(
        SavePlayerInput(
            tribeId = tribeId.value,
            playerId = element.id,
            name = element.name,
            email = element.email,
            badge = "${element.badge}",
            callSignAdjective = element.callSignAdjective,
            callSignNoun = element.callSignNoun,
            imageURL = element.imageURL,
        )
    )
}
