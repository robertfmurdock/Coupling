package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SaveTribeCommand(val tribe: Party) : SimpleSuspendAction<SaveTribeCommandDispatcher, Unit> {
    override val performFunc = link(SaveTribeCommandDispatcher::perform)
}

interface SaveTribeCommandDispatcher : TribeSaveSyntax {
    suspend fun perform(command: SaveTribeCommand) = command.tribe.save()
}
