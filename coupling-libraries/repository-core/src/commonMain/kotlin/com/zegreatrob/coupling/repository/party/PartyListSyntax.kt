package com.zegreatrob.coupling.repository.party

interface PartyListSyntax : PartyRecordSyntax {
    suspend fun getParties() = getPartyRecords().map { it.data }
}
