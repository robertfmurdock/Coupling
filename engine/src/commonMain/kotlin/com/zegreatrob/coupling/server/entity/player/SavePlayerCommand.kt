package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer

data class SavePlayerCommand(val tribeIdPlayer: TribeIdPlayer)

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax {

    suspend fun SavePlayerCommand.perform() = tribeIdPlayer.save().let { tribeIdPlayer.player }

}
