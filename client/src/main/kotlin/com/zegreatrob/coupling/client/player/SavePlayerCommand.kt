package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax

data class SavePlayerCommand(val tribeId: TribeId, val player: Player) :
    SimpleSuspendAction<SavePlayerCommandDispatcher, Unit> {
    override val performFunc = link(SavePlayerCommandDispatcher::perform)
}

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax {
    suspend fun perform(command: SavePlayerCommand) = command.tribeIdPlayer().save().successResult()

    private fun SavePlayerCommand.tribeIdPlayer() = tribeId.with(player)
}
