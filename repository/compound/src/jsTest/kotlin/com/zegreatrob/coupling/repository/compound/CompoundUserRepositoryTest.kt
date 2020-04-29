package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.MemoryUserRepository
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

@Suppress("unused")
class CompoundUserRepositoryTest : UserRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (UserRepository, User) -> Unit) {
        val stubUser = stubUser()

        val repository1 = MemoryUserRepository(stubUser.id, clock)
        val repository2 = MemoryUserRepository(stubUser.id, clock)

        val compoundRepo = CompoundUserRepository(repository1, repository2)
        handler(compoundRepo, stubUser)
    }

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