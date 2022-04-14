package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartySaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SaveTribeCommand(val tribe: Party) : SimpleSuspendAction<SavePartyCommandDispatcher, Unit> {
    override val performFunc = link(SavePartyCommandDispatcher::perform)
}

interface SavePartyCommandDispatcher : PartySaveSyntax {
    suspend fun perform(command: SaveTribeCommand) = command.tribe.save()
}
