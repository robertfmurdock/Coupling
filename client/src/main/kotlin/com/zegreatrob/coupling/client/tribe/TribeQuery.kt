package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getTribeAsync
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await

data class TribeQuery(val tribeId: TribeId, val coupling: Coupling)

interface TribeQueryDispatcher {
    suspend fun TribeQuery.perform() = coupling.getTribeAsync(tribeId).await()
}
