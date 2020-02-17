package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import stubTribeId
import stubUser

@Suppress("unused")
class MemoryPinRepositoryTest : PinRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (PinRepository, TribeId, User) -> Unit) {
        val user = stubUser()
        handler(MemoryPinRepository(user.email, clock), stubTribeId(), user)
    }
}
