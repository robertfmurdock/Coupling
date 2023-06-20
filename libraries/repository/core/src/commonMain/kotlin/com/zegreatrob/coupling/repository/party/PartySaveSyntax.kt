package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyDetails

interface PartySaveSyntax {
    val partyRepository: PartyDetailsSave
    suspend fun PartyDetails.save() = partyRepository.save(this)
}
