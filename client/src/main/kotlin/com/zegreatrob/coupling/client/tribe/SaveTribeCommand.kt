package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax

data class SaveTribeCommand(val tribe: Tribe) : SimpleSuspendAction<SaveTribeCommandDispatcher, Unit> {
    override val perform = link(SaveTribeCommandDispatcher::perform)
}

interface SaveTribeCommandDispatcher : TribeSaveSyntax {
    suspend fun perform(command: SaveTribeCommand) = command.tribe.save().successResult()
}
