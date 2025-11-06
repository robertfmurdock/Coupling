package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

interface PartyRecordSyntax {
    val partyRepository: PartyListGet
    suspend fun getPartyRecords(partyIds: Set<PartyId>) = partyRepository.loadParties(partyIds)
}
