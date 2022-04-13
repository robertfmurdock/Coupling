package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.party.PartyId

interface TribeIdGetSyntax : TribeIdGetRecordSyntax {
    suspend fun PartyId.get() = loadRecord()?.data
}
