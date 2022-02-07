package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

data class SavePlayerCommand(val player: Player) : SimpleSuspendResultAction<SavePlayerCommandDispatcher, Player> {
    override val performFunc = link(SavePlayerCommandDispatcher::perform)
}

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax, CurrentTribeIdSyntax {

    suspend fun perform(command: SavePlayerCommand) = command.sldkfjldksjf().successResult()

    private suspend fun SavePlayerCommand.sldkfjldksjf() = currentTribeId.with(player)
        .apply { save() }
        .run { player }

}
