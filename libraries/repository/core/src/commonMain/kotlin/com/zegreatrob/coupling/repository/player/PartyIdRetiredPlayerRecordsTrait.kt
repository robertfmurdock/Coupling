package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdRetiredPlayerRecordsTrait {
    val playerRepository: PlayerListGetDeleted
    suspend fun PartyId.loadRetiredPlayerRecords() = playerRepository.getDeleted(this)
}
