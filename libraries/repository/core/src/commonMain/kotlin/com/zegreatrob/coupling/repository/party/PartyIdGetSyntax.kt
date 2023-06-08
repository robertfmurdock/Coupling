package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdGetSyntax : PartyIdLoadSyntax {
    suspend fun PartyId.get() = load()?.data
}
