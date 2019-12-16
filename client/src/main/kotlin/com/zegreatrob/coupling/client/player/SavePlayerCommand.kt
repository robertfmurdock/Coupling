package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class SavePlayerCommand(val tribeId: TribeId, val player: Player) : Action

interface SavePlayerCommandDispatcher : ActionLoggingSyntax,
    TribeIdPlayerSaveSyntax {
    suspend fun SavePlayerCommand.perform() = logAsync { TribeIdPlayer(tribeId, player).save() }
}
