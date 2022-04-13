package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyId

interface PartyRetiredPlayersSyntax : PartyRetiredPlayerRecordsSyntax {
    suspend fun PartyId.loadRetiredPlayers() = loadRetiredPlayerRecords().map { it.data.element }
}
