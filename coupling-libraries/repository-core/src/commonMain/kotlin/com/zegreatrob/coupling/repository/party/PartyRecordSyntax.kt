package com.zegreatrob.coupling.repository.party

interface PartyRecordSyntax {
    val partyRepository: PartyListGet
    suspend fun getPartyRecords() = partyRepository.getParties()
}