package com.zegreatrob.coupling.repository.memory

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator

@Suppress("unused")
class MemoryUserRepositoryTest : UserRepositoryValidator<MemoryUserRepositoryTest.SharedContext> {

    data class SharedContext(
        override val repository: MemoryUserRepository,
        override val clock: MagicClock,
        override val user: User
    ) : UserRepositoryValidator.SharedContext

    override suspend fun withRepository(clock: MagicClock, handler: suspend (UserRepository, User) -> Unit) {
        val (repository, _, user) = setupRepository(clock)
        handler(repository, user)
    }

    override suspend fun setupRepository(clock: MagicClock): SharedContext {
        val email = "${uuid4()}@mail.com"
        val id = "${uuid4()}"
        val user = User(id, email, emptySet())
        val repository = MemoryUserRepository(id, clock)
        return SharedContext(repository, clock, user)
    }

    override suspend fun SharedContext.teardown() = Unit

}