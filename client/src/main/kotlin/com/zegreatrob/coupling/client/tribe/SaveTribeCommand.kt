package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeSaveSyntax

data class SaveTribeCommand(val tribe: KtTribe) : Action

interface SaveTribeCommandDispatcher : ActionLoggingSyntax, TribeSaveSyntax {

    suspend fun SaveTribeCommand.perform() = logAsync { tribe.save() }

}
