package com.zegreatrob.coupling.model.tribe

interface TribeRepository : TribeGet, TribeListGet, TribeSave, TribeDelete

interface TribeSave {
    suspend fun save(tribe: KtTribe)
}

interface TribeDelete {
    suspend fun delete(tribeId: TribeId): Boolean
}

interface TribeGet {
    suspend fun getTribe(tribeId: TribeId): KtTribe?
}

interface TribeListGet {
    suspend fun getTribes(): List<KtTribe>
}