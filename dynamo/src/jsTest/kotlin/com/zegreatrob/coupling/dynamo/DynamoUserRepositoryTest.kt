package com.zegreatrob.coupling.dynamo

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator

@Suppress("unused")
class DynamoUserRepositoryTest :
    UserRepositoryValidator {
    override suspend fun withRepository(handler: suspend (UserRepository, User) -> Unit) {
        val email = "${uuid4()}"
        handler(DynamoUserRepository(email), User("${uuid4()}", email, emptySet()))
    }
}