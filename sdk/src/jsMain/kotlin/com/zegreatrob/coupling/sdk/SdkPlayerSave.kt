package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.pairsToJson
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
            .plus(Pair<String, Any?>("playerId", element.id))
            .plus(Pair<String, Any?>("name", element.name))
            .plus(Pair<String, Any?>("email", element.email))
            .plus(Pair<String, Any?>("badge", "${element.badge}"))
            .plus(Pair<String, Any?>("callSignAdjective", element.callSignAdjective))
            .plus(Pair<String, Any?>("callSignNoun", element.callSignNoun))
            .plus(Pair<String, Any?>("imageURL", element.imageURL))
            .pairsToJson()
    )
}
