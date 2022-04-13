package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyId

interface TribeIdRetiredPlayersSyntax : TribeIdRetiredPlayerRecordsSyntax {
    suspend fun PartyId.loadRetiredPlayers() = loadRetiredPlayerRecords().map { it.data.element }
}
