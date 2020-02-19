package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import stubTribeId
import stubUser

@Suppress("unused")
class MemoryPlayerRepositoryTest : PlayerEmailRepositoryValidator<MemoryPlayerRepository> {
    override suspend fun withRepository(handler: suspend (MemoryPlayerRepository, TribeId, User) -> Unit) {
        val user = stubUser()
        handler(MemoryPlayerRepository(user.email, TimeProvider), stubTribeId(), user)
    }
}
