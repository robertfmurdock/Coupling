package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class SavePlayerCommand(val tribeId: TribeId, val player: Player) : Action

interface SavePlayerCommandDispatcher : ActionLoggingSyntax, PlayerSaveSyntax {

    suspend fun SavePlayerCommand.perform() = logAsync { saveAsync(tribeId, player) }

}
