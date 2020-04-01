package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser

@Suppress("unused")
class MemoryPlayerRepositoryTest : PlayerEmailRepositoryValidator<MemoryPlayerRepository> {
    override suspend fun withRepository(
        clock: MagicClock,
        handler: suspend (MemoryPlayerRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser()
        handler(MemoryPlayerRepository(user.email, clock), stubTribeId(), user)
    }
}
