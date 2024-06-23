package com.zegreatrob.coupling.repository

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId

interface BoostRepository :
    BoostGet,
    BoostSave,
    BoostDelete,
    BoostGetByPartyId

interface ExtendedBoostRepository :
    BoostRepository,
    BoostGetByPartyId

interface BoostGetByPartyId {
    suspend fun getByPartyId(partyId: PartyId): Record<Boost>?
}

fun interface BoostSave {
    suspend fun save(boost: Boost)
}

interface BoostGet {
    suspend fun get(): Record<Boost>?
}

interface BoostDelete {
    suspend fun deleteIt()
}
