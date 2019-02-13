package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer

data class SavePlayerCommand(val tribeIdPlayer: TribeIdPlayer) : Action

interface SavePlayerCommandDispatcher : ActionLoggingSyntax, TribeIdPlayerSaveSyntax {

    suspend fun SavePlayerCommand.perform() = logAsync { tribeIdPlayer.save().let { tribeIdPlayer.player } }

}
