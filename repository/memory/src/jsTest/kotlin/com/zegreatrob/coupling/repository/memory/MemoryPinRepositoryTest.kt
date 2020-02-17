package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import stubTribeId
import stubUser

@Suppress("unused")
class MemoryPinRepositoryTest : PinRepositoryValidator {
    override suspend fun withRepository(handler: suspend (PinRepository, TribeId) -> Unit) {
        val user = stubUser()
        handler(MemoryPinRepository(user.email), stubTribeId())
    }
}
