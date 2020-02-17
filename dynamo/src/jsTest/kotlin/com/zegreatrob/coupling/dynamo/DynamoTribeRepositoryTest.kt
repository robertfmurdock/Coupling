package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import stubUser

@Suppress("unused")
class DynamoTribeRepositoryTest :
    TribeRepositoryValidator {

    override suspend fun withRepository(handler: suspend (TribeRepository) -> Unit) {
        val user = stubUser()
        val repository = DynamoTribeRepository(user.email, TimeProvider)
        handler(repository)
    }

}