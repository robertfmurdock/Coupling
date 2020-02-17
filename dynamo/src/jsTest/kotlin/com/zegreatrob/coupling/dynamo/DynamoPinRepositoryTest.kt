package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import stubTribeId

@Suppress("unused")
class DynamoPinRepositoryTest :
    PinRepositoryValidator {
    override suspend fun withRepository(handler: suspend (PinRepository, TribeId) -> Unit) {
        handler(DynamoPinRepository(), stubTribeId())
    }
}