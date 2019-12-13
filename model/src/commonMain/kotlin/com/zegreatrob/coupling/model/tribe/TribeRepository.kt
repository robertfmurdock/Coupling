package com.zegreatrob.coupling.model.tribe

interface TribeRepository : TribeGet, TribeListGet, TribeSave, TribeDelete

interface TribeSave {
    suspend fun save(tribe: Tribe)
}

interface TribeDelete {
    suspend fun delete(tribeId: TribeId): Boolean
}

interface TribeGet {
    suspend fun getTribe(tribeId: TribeId): Tribe?
}

interface TribeListGet {
    suspend fun getTribes(): List<Tribe>
}