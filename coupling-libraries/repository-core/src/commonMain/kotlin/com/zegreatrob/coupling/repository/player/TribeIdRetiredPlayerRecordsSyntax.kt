package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.tribe.PartyId

interface TribeIdRetiredPlayerRecordsSyntax {
    val playerRepository: PlayerListGetDeleted
    suspend fun PartyId.loadRetiredPlayerRecords() = playerRepository.getDeleted(this)
}