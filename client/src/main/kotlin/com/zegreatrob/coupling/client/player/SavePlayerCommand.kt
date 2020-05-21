package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax

data class SavePlayerCommand(val tribeId: TribeId, val player: Player) :
    SimpleSuspendResultAction<SavePlayerCommandDispatcher, Unit> {
    override val performFunc = link(SavePlayerCommandDispatcher::perform)
}

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax {
    suspend fun perform(command: SavePlayerCommand) = command.tribeIdPlayer().save().successResult()

    private fun SavePlayerCommand.tribeIdPlayer() = tribeId.with(player)
}
