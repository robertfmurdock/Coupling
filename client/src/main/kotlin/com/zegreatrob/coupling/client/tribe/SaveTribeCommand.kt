package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SaveTribeCommand(val tribe: Tribe) : SimpleSuspendAction<SaveTribeCommandDispatcher, Unit> {
    override val performFunc = link(SaveTribeCommandDispatcher::perform)
}

interface SaveTribeCommandDispatcher : TribeSaveSyntax {
    suspend fun perform(command: SaveTribeCommand) = command.tribe.save()
}
