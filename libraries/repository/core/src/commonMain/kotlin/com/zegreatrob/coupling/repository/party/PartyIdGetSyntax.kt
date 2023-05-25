package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdGetSyntax : PartyIdGetRecordSyntax {
    suspend fun PartyId.get() = loadRecord()?.data
}
