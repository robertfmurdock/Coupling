package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.pairsToJson
import com.zegreatrob.coupling.json.plus
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.repository.player.PlayerSave
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

    private fun TribeIdPlayer.savePlayerInput() = json("tribeId" to id.value).add(
        emptyArray<Pair<String, Any?>>()
            .plus("playerId", element.id)
            .plus("name", element.name)
            .plus("email", element.email)
            .plus("badge", "${element.badge}")
            .plus("callSignAdjective", element.callSignAdjective)
            .plus("callSignNoun", element.callSignNoun)
            .plus("imageURL", element.imageURL)
            .pairsToJson()
    )
}
