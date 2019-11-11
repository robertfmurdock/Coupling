package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeListSyntax

object TribeListQuery : Action

interface TribeListQueryDispatcher : ActionLoggingSyntax, TribeListSyntax {
    suspend fun TribeListQuery.perform() = logAsync { getTribes() }
}
