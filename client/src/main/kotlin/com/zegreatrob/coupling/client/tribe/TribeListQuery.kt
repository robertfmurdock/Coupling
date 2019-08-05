package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax

object TribeListQuery : Action

interface TribeListQueryDispatcher : ActionLoggingSyntax, GetTribeListSyntax {
    suspend fun TribeListQuery.perform() = logAsync { getTribeListAsync().await() }
}
