package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.repository.party.PartySaveSyntax

interface ClientSavePartyCommandDispatcher : PartySaveSyntax, SavePartyCommand.Dispatcher {
    override suspend fun perform(command: SavePartyCommand) = command.party.save()
}
