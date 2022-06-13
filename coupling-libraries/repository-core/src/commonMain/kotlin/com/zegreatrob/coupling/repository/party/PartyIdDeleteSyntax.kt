package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdDeleteSyntax {

    val partyRepository: PartyDelete

    suspend fun PartyId.deleteIt() = partyRepository.deleteIt(this)
}
