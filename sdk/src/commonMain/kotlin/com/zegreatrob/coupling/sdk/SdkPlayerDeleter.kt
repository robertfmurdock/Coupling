package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.player.PlayerDelete

interface SdkPlayerDeleter : PlayerDelete, GqlSyntax, GraphQueries {
    override suspend fun deletePlayer(tribeId: PartyId, playerId: String) = doQuery(
        mutations.deletePlayer,
        mapOf("tribeId" to tribeId.value, "playerId" to playerId),
        "deletePlayer"
    ) { it: Boolean? -> it } ?: false
}
