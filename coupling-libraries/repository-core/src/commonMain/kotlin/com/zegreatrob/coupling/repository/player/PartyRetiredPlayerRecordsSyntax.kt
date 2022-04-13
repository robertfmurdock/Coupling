package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyId

interface PartyRetiredPlayerRecordsSyntax {
    val playerRepository: PlayerListGetDeleted
    suspend fun PartyId.loadRetiredPlayerRecords() = playerRepository.getDeleted(this)
}