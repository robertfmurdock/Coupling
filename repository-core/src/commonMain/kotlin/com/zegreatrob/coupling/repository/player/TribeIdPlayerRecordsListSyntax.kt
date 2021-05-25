package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPlayerRecordsListSyntax {
    val playerRepository: PlayerListGet
    suspend fun TribeId.getPlayerRecords() = playerRepository.getPlayers(this)
}