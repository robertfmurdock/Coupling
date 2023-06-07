package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.player.PartyPlayerSaveSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

interface ServerSavePlayerCommandDispatcher :
    SavePlayerCommand.Dispatcher,
    PartyPlayerSaveSyntax,
    CurrentPartyIdSyntax {

    override suspend fun perform(command: SavePlayerCommand) = command.save()

    private suspend fun SavePlayerCommand.save() = currentPartyId.with(player)
        .apply { save() }
        .run { VoidResult.Accepted }
}
