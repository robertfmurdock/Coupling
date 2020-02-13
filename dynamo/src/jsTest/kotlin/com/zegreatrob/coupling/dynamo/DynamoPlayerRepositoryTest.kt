package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.repositoryvalidation.PlayerRepositoryValidator
import stubTribeId

@Suppress("unused")
class DynamoPlayerRepositoryTest : PlayerRepositoryValidator {

    override suspend fun withRepository(handler: suspend (PlayerRepository, TribeId) -> Unit) {
        handler(DynamoPlayerRepository(), stubTribeId())
    }

    override fun deletedPlayersShowUpInGetDeleted(): Any? {
        TODO()
    }

    override fun deletedThenBringBackThenDeletedWillShowUpOnceInGetDeleted(): Any? {
        TODO()
    }

}