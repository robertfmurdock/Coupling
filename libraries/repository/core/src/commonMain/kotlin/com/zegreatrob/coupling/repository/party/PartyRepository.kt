package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration

interface PartyRepository :
    PartyDetailsGet,
    PartyIntegrationGet,
    PartyListGet,
    PartyDetailsSave,
    PartyIntegrationSave,
    PartyDelete

fun interface PartyDetailsSave {
    suspend fun save(party: PartyDetails)
}

fun interface PartyIntegrationSave {
    suspend fun save(integration: PartyElement<PartyIntegration>)
}

fun interface PartyDelete {
    suspend fun deleteIt(partyId: PartyId): Boolean
}

fun interface PartyDetailsGet {
    suspend fun getDetails(partyId: PartyId): Record<PartyDetails>?
}

fun interface PartyIntegrationGet {
    suspend fun getIntegration(partyId: PartyId): Record<PartyIntegration>?
}

fun interface PartyListGet {
    suspend fun loadParties(partyIds: Set<PartyId>): List<Record<PartyDetails>>
}
