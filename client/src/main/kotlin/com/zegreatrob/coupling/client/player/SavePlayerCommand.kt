package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax

data class SavePlayerCommand(val tribeId: TribeId, val player: Player) :
    SuspendAction<SavePlayerCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: SavePlayerCommandDispatcher) = with(dispatcher) { perform() }
}

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax {
    suspend fun SavePlayerCommand.perform() = tribeId.with(player).save().successResult()
}
