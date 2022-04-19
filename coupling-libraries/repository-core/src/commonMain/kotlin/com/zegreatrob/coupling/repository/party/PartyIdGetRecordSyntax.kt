package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdGetRecordSyntax {
    val partyRepository: PartyGet
    suspend fun PartyId.loadRecord() = partyRepository.getPartyRecord(this)
}
