package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdLoadSyntax {
    val partyRepository: PartyGet
    suspend fun PartyId.load() = partyRepository.getPartyRecord(this)
}
