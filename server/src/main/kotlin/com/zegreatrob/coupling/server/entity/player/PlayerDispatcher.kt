package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.coupling.server.action.player.SavePlayerCommandDispatcher

interface PlayerDispatcher : SavePlayerCommandDispatcher, DeletePlayerCommandDispatcher {
    override val playerRepository: PlayerRepository
}
