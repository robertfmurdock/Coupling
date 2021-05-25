package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeRepository : TribeGet,
    TribeListGet,
    TribeSave,
    TribeDelete

interface TribeSave {
    suspend fun save(tribe: Tribe)
}

interface TribeDelete {
    suspend fun delete(tribeId: TribeId): Boolean
}

interface TribeGet {
    suspend fun getTribeRecord(tribeId: TribeId): Record<Tribe>?
}

interface TribeListGet {
    suspend fun getTribes(): List<Record<Tribe>>
}
