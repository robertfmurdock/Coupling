package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.tribe.KtTribe

data class SaveTribeCommand(val tribe: KtTribe) : Action

interface SaveTribeCommandDispatcher : ActionLoggingSyntax, TribeSaveSyntax {

    suspend fun SaveTribeCommand.perform() = logAsync { tribe.saveAsync().await() }

}
