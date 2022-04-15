package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartySaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePartyCommand(val party: Party) : SimpleSuspendAction<SavePartyCommandDispatcher, Unit> {
    override val performFunc = link(SavePartyCommandDispatcher::perform)
}

interface SavePartyCommandDispatcher : PartySaveSyntax {
    suspend fun perform(command: SavePartyCommand) = command.party.save()
}
