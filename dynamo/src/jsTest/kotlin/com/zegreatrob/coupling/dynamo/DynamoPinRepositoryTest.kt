package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import stubTribeId
import stubUser

@Suppress("unused")
class DynamoPinRepositoryTest : PinRepositoryValidator {
    override suspend fun withRepository(handler: suspend (PinRepository, TribeId) -> Unit) {
        val user = stubUser()
        handler(DynamoPinRepository(user.email, TimeProvider), stubTribeId())
    }
}