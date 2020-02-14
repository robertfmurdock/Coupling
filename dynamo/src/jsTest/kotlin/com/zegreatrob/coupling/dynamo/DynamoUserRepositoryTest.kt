package com.zegreatrob.coupling.dynamo

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repositoryvalidation.UserRepositoryValidator

@Suppress("unused")
class DynamoUserRepositoryTest : UserRepositoryValidator {
    override suspend fun withRepository(handler: suspend (UserRepository, User) -> Unit) {
        handler(DynamoUserRepository(), User("${uuid4()}", "${uuid4()}", emptySet()))
    }

    override fun saveUserRepeatedlyGetsLatest(): Any? {
        TODO()
    }

    override fun saveUserThenGetWillContainAllSavedValues(): Any? {
        TODO()
    }

}