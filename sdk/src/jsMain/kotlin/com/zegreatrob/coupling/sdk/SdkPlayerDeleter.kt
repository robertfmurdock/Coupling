package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerDelete

interface SdkPlayerDeleter : PlayerDelete, GqlSyntax {
    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = performQuery(
        Mutations.deletePlayer,
        mapOf("tribeId" to tribeId.value, "playerId" to playerId),
        "deletePlayer"
    ) { it: Boolean -> it } ?: false
}
