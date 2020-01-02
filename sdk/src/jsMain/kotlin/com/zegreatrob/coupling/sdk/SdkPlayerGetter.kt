package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.playerJsonKeys
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerGetter
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface SdkPlayerGetter : PlayerGetter, AxiosSyntax {
    override suspend fun getPlayers(tribeId: TribeId): List<Player> = axios.post(
        "/api/graphql", json(
            "query" to "{ tribe(id: \"${tribeId.value}\") { playerList {${playerJsonKeys.joinToString(",")}} } }"
        )
    )
        .then<List<Player>?> {
            it.data.data.tribe?.playerList.unsafeCast<Array<Json>?>()
                ?.map(Json::toPlayer)
        }
        .await()
        .let { it ?: throw Exception("Tribe not found.") }
}