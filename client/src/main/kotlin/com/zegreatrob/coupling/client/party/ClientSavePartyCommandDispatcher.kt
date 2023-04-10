package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.repository.party.PartySaveSyntax

interface ClientSavePartyCommandDispatcher : PartySaveSyntax, SavePartyCommand.Dispatcher {
    override suspend fun perform(command: SavePartyCommand) = command.party.save()
}
