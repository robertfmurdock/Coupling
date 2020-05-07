package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.player.TribeIdPlayerId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.TribeIdPlayerIdDeleteSyntax
import com.zegreatrob.coupling.server.action.SuspendAction
import com.zegreatrob.coupling.server.action.deletionResult

data class DeletePlayerCommand(val tribeId: TribeId, val playerId: String) :
    SuspendAction<DeletePlayerCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: DeletePlayerCommandDispatcher) = with(dispatcher) { perform() }
}

interface DeletePlayerCommandDispatcher : TribeIdPlayerIdDeleteSyntax {
    suspend fun DeletePlayerCommand.perform() = TribeIdPlayerId(tribeId, playerId).run { deletePlayer() }
        .deletionResult("Player")
}
