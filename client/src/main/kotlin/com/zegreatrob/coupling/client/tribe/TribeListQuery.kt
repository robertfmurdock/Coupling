package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.sdk.GetTribeListSyntax
import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax

object TribeListQuery : Action

interface TribeListQueryDispatcher : ActionLoggingSyntax, GetTribeListSyntax {
    suspend fun TribeListQuery.perform() = logAsync { getTribeListAsync().await() }
}
