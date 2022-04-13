package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyId

interface TribeIdPlayerRecordsListSyntax {
    val playerRepository: PlayerListGet
    suspend fun PartyId.getPlayerRecords() = playerRepository.getPlayers(this)
}