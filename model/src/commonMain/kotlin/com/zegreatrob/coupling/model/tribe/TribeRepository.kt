package com.zegreatrob.coupling.model.tribe

import kotlinx.coroutines.Deferred

interface TribeRepository : TribeGet, TribeListGet, TribeSave, TribeDelete

interface TribeSave {
    suspend fun save(tribe: KtTribe)
}

interface TribeDelete {
    suspend fun delete(tribeId: TribeId): Boolean
}

interface TribeGet {
    fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe?>
}

interface TribeListGet {
    fun getTribesAsync(): Deferred<List<KtTribe>>
}