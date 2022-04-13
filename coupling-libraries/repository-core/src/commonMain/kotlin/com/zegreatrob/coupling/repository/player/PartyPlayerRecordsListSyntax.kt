package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyId

interface PartyPlayerRecordsListSyntax {
    val playerRepository: PlayerListGet
    suspend fun PartyId.getPlayerRecords() = playerRepository.getPlayers(this)
}