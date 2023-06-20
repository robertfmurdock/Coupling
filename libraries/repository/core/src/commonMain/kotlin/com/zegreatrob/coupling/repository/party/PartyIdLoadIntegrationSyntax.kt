package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdLoadIntegrationSyntax {

    val partyRepository: PartyIntegrationGet

    suspend fun PartyId.loadIntegration() = partyRepository.getIntegration(this)?.data
}
