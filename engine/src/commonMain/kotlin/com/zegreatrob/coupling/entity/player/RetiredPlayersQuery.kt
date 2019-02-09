package com.zegreatrob.coupling.entity.player

import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class RetiredPlayersQuery(val tribeId: TribeId)

interface RetiredPlayersQueryDispatcher : TribeIdRetiredPlayersSyntax {
    suspend fun RetiredPlayersQuery.perform() = tribeId.loadRetiredPlayers()
}
