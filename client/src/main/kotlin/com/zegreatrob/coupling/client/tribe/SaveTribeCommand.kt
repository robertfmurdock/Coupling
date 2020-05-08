package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax

data class SaveTribeCommand(val tribe: Tribe) : SuspendAction<SaveTribeCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: SaveTribeCommandDispatcher) = with(dispatcher) { perform() }
}

interface SaveTribeCommandDispatcher : TribeSaveSyntax {
    suspend fun SaveTribeCommand.perform() = tribe.save().successResult()
}
