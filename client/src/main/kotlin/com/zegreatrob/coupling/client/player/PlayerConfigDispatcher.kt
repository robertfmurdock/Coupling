package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.repository.player.PlayerRepository

interface PlayerConfigDispatcher :
    SavePlayerCommandDispatcher,
    DeletePlayerCommandDispatcher {
    override val playerRepository: PlayerRepository
}
