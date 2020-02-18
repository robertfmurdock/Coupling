package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import stubUser

@Suppress("unused")
class DynamoTribeRepositoryTest : TribeRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit) {
        val user = stubUser()
        val repository = DynamoTribeRepository(user.email, clock)
        handler(repository, user)
    }

}