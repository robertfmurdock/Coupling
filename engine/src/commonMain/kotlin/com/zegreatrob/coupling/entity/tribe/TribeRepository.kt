package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

interface TribeRepository : TribeGet, TribeListGet, TribeSave

interface TribeSave {
    suspend fun save(tribe: KtTribe)
}

interface TribeGet {
    fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe?>
}

interface TribeListGet {
    fun getTribesAsync(): Deferred<List<KtTribe>>
}