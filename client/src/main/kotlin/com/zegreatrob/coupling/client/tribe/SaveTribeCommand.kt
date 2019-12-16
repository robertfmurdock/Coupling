package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax

data class SaveTribeCommand(val tribe: Tribe) : Action

interface SaveTribeCommandDispatcher : ActionLoggingSyntax,
    TribeSaveSyntax {

    suspend fun SaveTribeCommand.perform() = logAsync { tribe.save() }

}
