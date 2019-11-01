package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.sdk.PlayerSaveSyntax
import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId

data class SavePlayerCommand(val tribeId: TribeId, val player: Player) : Action

interface SavePlayerCommandDispatcher : ActionLoggingSyntax, PlayerSaveSyntax {

    suspend fun SavePlayerCommand.perform() = logAsync { saveAsync(tribeId, player) }

}
