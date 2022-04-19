package com.zegreatrob.coupling.repository.memory

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.time.ExperimentalTime

@Suppress("unused")
@ExperimentalTime
class MemoryUserRepositoryTest : UserRepositoryValidator<MemoryUserRepository> {

    override val repositorySetup: TestTemplate<SharedContext<MemoryUserRepository>>
        get() = asyncTestTemplate(sharedSetup = {
            val clock = MagicClock()
            val email = "${uuid4()}@mail.com"
            val id = "${uuid4()}"
            val user = User(id, email, emptySet())
            val repository = MemoryUserRepository(id, clock)
            SharedContextData(repository, clock, user)
        })
}
