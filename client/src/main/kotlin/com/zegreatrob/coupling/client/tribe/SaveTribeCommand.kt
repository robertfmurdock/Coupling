package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax

data class SaveTribeCommand(val tribe: Tribe) :
    SimpleSuspendResultAction<SaveTribeCommandDispatcher, Unit> {
    override val performFunc = link(SaveTribeCommandDispatcher::perform)
}

interface SaveTribeCommandDispatcher : TribeSaveSyntax {
    suspend fun perform(command: SaveTribeCommand) = command.tribe.save().successResult()
}
