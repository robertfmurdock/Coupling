package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getTribeListAsync
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import kotlinx.coroutines.await

data class TribeListQuery(val coupling: Coupling) : Action

interface TribeListQueryDispatcher : ActionLoggingSyntax {
    suspend fun TribeListQuery.perform() = logAsync { coupling.getTribeListAsync().await() }
}
