package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getTribeListAsync
import kotlinx.coroutines.await

data class TribeListQuery(val coupling: Coupling)

interface TribeListQueryDispatcher {
    suspend fun TribeListQuery.perform() = coupling.getTribeListAsync().await()
}
