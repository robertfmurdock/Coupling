package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.TribeIdPlayerSaveSyntax

data class SavePlayerCommand(val tribeIdPlayer: TribeIdPlayer) : Action

interface SavePlayerCommandDispatcher : ActionLoggingSyntax, TribeIdPlayerSaveSyntax {

    suspend fun SavePlayerCommand.perform() = logAsync { tribeIdPlayer.save().let { tribeIdPlayer.player } }

}
