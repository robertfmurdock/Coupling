package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator

@Suppress("unused")
class MemoryTribeRepositoryTest : TribeRepositoryValidator {
    override suspend fun withRepository(handler: suspend (TribeRepository) -> Unit) {
        handler(MemoryTribeRepository())
    }
}