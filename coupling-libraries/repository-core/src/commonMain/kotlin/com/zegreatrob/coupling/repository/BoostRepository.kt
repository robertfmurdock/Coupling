package com.zegreatrob.coupling.repository

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId

interface BoostRepository : BoostGet, BoostSave, BoostDelete

interface ExtendedBoostRepository : BoostRepository, BoostGetByPartyId

interface BoostGetByPartyId {
    suspend fun getByPartyId(partyId: PartyId): Record<Boost>?
}

interface BoostSave {
    suspend fun save(boost: Boost)
}

interface BoostGet {
    suspend fun get(): Record<Boost>?
}

interface BoostDelete {
    suspend fun delete()
}
