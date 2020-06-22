package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.MemoryUserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

@Suppress("unused")
class CompoundUserRepositoryTest : UserRepositoryValidator<CompoundUserRepository> {
    class SharedContext(
        override val repository: CompoundUserRepository,
        override val clock: MagicClock,
        override val user: User
    ) : UserRepositoryValidator.SharedContext<CompoundUserRepository>

    override val userRepositorySetup: TestTemplate<UserRepositoryValidator.SharedContext<CompoundUserRepository>>
        get() = asyncTestTemplate<UserRepositoryValidator.SharedContext<CompoundUserRepository>>(sharedSetup = {
            val clock = MagicClock()
            val stubUser = stubUser()

            val repository1 = MemoryUserRepository(stubUser.id, clock)
            val repository2 = MemoryUserRepository(stubUser.id, clock)

            val compoundRepo = CompoundUserRepository(repository1, repository2)
            SharedContext(compoundRepo, clock, stubUser)
        }, sharedTeardown = {})

    @Test
    fun saveWillSaveToSecondRepositoryAsWell() = asyncSetup(object {
        val user = stubUser()

        val repository1 = MemoryUserRepository(user.id, TimeProvider)
        val repository2 = MemoryUserRepository(user.id, TimeProvider)

        val compoundRepo = CompoundUserRepository(repository1, repository2)
    }) exercise {
        compoundRepo.save(user)
    } verify {
        repository2.getUser()?.data
            .assertIsEqualTo(user)
    }

}