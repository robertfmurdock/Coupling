package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository

class CompoundPlayerRepository(
    private val repository1: PlayerEmailRepository,
    private val repository2: PlayerEmailRepository
) :
    PlayerEmailRepository by repository1 {

    override suspend fun save(tribeIdPlayer: TribeIdPlayer) = arrayOf(repository1, repository2).forEach {
        it.save(tribeIdPlayer)
    }

    override suspend fun deletePlayer(tribeId: TribeId, playerId: String) = repository1.deletePlayer(tribeId, playerId)
        .also { repository2.deletePlayer(tribeId, playerId) }

}
