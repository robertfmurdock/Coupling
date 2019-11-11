package com.zegreatrob.coupling.model.tribe

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

interface TribeRepository : TribeGet, TribeListGet, TribeSave, TribeDelete

interface TribeSave {
    suspend fun save(tribe: KtTribe)
}

interface TribeDelete {
    suspend fun delete(tribeId: TribeId): Boolean
}

interface TribeGet {
    fun CoroutineScope.getTribeAsync(tribeId: TribeId): Deferred<KtTribe?>
}

interface TribeListGet {
    suspend fun getTribes(): List<KtTribe>
}