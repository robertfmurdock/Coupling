package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class TribeQuery(val tribeId: TribeId) : Action

interface TribeQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax {
    suspend fun TribeQuery.perform() = logAsync { getTribeAsync(tribeId).await() }
}
