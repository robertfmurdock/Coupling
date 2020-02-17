package com.zegreatrob.coupling.repository.memory;

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator

@Suppress("unused")
class MemoryUserRepositoryTest : UserRepositoryValidator {
    override suspend fun withRepository(handler: suspend (UserRepository, User) -> Unit) {
        val email = "${uuid4()}@mail.com"
        val repository = MemoryUserRepository(email)
        val id = "${uuid4()}"
        val user = User(id, email, emptySet())
        handler(repository, user)
    }
}