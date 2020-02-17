package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.repository.validation.PlayerRepositoryValidator
import stubTribeId

@Suppress("unused")
class MemoryPlayerRepositoryTest : PlayerRepositoryValidator {
    override suspend fun withRepository(handler: suspend (PlayerRepository, TribeId) -> Unit) {
        handler(MemoryPlayerRepository(), stubTribeId())
    }
}
