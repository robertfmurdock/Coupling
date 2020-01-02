package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerGetter
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PlayerList
import kotlinx.coroutines.await
import kotlin.js.Json

interface SdkPlayerGetter : PlayerGetter, TribeGQLSyntax {
    override suspend fun getPlayers(tribeId: TribeId): List<Player> = performTribeGQLQuery(tribeId, listOf(PlayerList))
        .then {
            it[PlayerList].unsafeCast<Array<Json>?>()
                ?.map(Json::toPlayer)
        }
        .await()
        .let { it ?: throw Exception("Tribe not found.") }
}
