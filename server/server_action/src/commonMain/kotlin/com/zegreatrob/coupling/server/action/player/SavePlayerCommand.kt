package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax

data class SavePlayerCommand(val tribeIdPlayer: TribeIdPlayer) :
    SimpleSuspendResultAction<SavePlayerCommandDispatcher, Player> {
    override val performFunc = link(SavePlayerCommandDispatcher::perform)
}

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax {

    suspend fun perform(command: SavePlayerCommand) = command.sldkfjldksjf().successResult()

    private suspend fun SavePlayerCommand.sldkfjldksjf() = tribeIdPlayer
        .apply { save() }
        .run { player }

}
