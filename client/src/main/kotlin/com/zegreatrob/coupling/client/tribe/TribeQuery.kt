package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getTribeAsync
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await

data class TribeQuery(val tribeId: TribeId, val coupling: Coupling) : Action

interface TribeQueryDispatcher : ActionLoggingSyntax {
    suspend fun TribeQuery.perform() = logAsync { coupling.getTribeAsync(tribeId).await() }
}
