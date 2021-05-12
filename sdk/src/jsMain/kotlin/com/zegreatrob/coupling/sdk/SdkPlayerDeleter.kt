package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerDelete
import com.zegreatrob.minjson.at
import kotlin.js.Json
import kotlin.js.json

interface SdkPlayerDeleter : PlayerDelete, GqlSyntax {
    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = performQuery(
        json(
            "query" to Mutations.deletePlayer,
            "variables" to json("input" to json("tribeId" to tribeId.value, "playerId" to playerId))
        )
    ).unsafeCast<Json>()
        .at<Boolean?>("/data/data/deletePlayer") ?: false
}