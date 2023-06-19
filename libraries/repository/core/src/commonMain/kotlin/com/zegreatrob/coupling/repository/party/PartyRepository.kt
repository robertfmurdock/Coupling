package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId

interface PartyRepository :
    PartyGet,
    PartyListGet,
    PartySave,
    PartyDelete

interface PartySave {
    suspend fun save(party: Party)
}

interface PartyDelete {
    suspend fun deleteIt(partyId: PartyId): Boolean
}

fun interface PartyGet {
    suspend fun getPartyRecord(partyId: PartyId): Record<Party>?
}

interface PartyListGet {
    suspend fun loadParties(): List<Record<Party>>
}
