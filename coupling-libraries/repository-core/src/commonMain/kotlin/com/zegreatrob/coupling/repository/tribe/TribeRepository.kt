package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId

interface TribeRepository : TribeGet,
    TribeListGet,
    TribeSave,
    TribeDelete

interface TribeSave {
    suspend fun save(tribe: Party)
}

interface TribeDelete {
    suspend fun delete(partyId: PartyId): Boolean
}

interface TribeGet {
    suspend fun getTribeRecord(partyId: PartyId): Record<Party>?
}

interface TribeListGet {
    suspend fun getTribes(): List<Record<Party>>
}
