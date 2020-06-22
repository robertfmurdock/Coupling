package com.zegreatrob.coupling.repository.memory

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryUserRepositoryTest : UserRepositoryValidator<MemoryUserRepository> {

    data class SharedContext(
        override val repository: MemoryUserRepository,
        override val clock: MagicClock,
        override val user: User
    ) : UserRepositoryValidator.SharedContext<MemoryUserRepository>

    override val userRepositorySetup: TestTemplate<UserRepositoryValidator.SharedContext<MemoryUserRepository>>
        get() = asyncTestTemplate<UserRepositoryValidator.SharedContext<MemoryUserRepository>>(sharedSetup = {
            val clock = MagicClock()
            val email = "${uuid4()}@mail.com"
            val id = "${uuid4()}"
            val user = User(id, email, emptySet())
            val repository = MemoryUserRepository(id, clock)
            SharedContext(repository, clock, user)
        }, sharedTeardown = {})

}