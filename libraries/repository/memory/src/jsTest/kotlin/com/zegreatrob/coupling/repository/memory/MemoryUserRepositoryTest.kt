package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotools.types.text.toNotBlankString
import kotlin.uuid.Uuid

@Suppress("unused")
class MemoryUserRepositoryTest : UserRepositoryValidator<MemoryUserRepository> {

    override val repositorySetup: TestTemplate<SharedContext<MemoryUserRepository>>
        get() = asyncTestTemplate(sharedSetup = {
            val clock = MagicClock()
            val email = "${Uuid.random()}@mail.com".toNotBlankString().getOrThrow()
            val id = UserId.new()
            val user = UserDetails(id, email, emptySet(), null, null)
            val repository = MemoryUserRepository(id, clock)
            SharedContextData(repository, clock, user)
        })
}
