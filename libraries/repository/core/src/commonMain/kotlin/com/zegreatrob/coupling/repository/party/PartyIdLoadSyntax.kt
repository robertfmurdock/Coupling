package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdLoadSyntax {
    val partyRepository: PartyDetailsGet
    suspend fun PartyId.load() = partyRepository.getDetails(this)
}
