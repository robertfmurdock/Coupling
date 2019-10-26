package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.core.entity.tribe.KtTribe
import com.zegreatrob.coupling.core.entity.tribe.TribeId
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