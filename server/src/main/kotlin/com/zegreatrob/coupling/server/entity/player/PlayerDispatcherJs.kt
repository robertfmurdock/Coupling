package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.repository.player.PlayerRepository

interface PlayerDispatcherJs : SavePlayerCommandDispatcherJs, DeletePlayerCommandDispatcherJs,
    RetiredPlayersQueryDispatcherJs {
    override val playerRepository: PlayerRepository
}
