package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax
import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult

data class SavePlayerCommand(val tribeIdPlayer: TribeIdPlayer) :
    SuspendAction<SavePlayerCommandDispatcher, Player> {
    override suspend fun execute(dispatcher: SavePlayerCommandDispatcher) = with(dispatcher) { perform() }
}

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax {

    suspend fun SavePlayerCommand.perform() = tribeIdPlayer.save().let { tribeIdPlayer.player }.successResult()

}
