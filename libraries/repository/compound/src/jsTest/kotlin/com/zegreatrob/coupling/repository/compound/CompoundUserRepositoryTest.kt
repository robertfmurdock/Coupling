package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.repository.memory.MemoryUserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.datetime.Clock
import kotlin.test.Test

@Suppress("unused")
class CompoundUserRepositoryTest : UserRepositoryValidator<CompoundUserRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<CompoundUserRepository>>(sharedSetup = {
        val clock = MagicClock()
        val stubUser = stubUser()

        val repository1 = MemoryUserRepository(stubUser.id, clock)
        val repository2 = MemoryUserRepository(stubUser.id, clock)

        val compoundRepo = CompoundUserRepository(repository1, repository2)
        SharedContextData(compoundRepo, clock, stubUser)
    })

    @Test
    fun saveWillSaveToSecondRepositoryAsWell() = asyncSetup(object {
        val user = stubUser()

        val repository1 = MemoryUserRepository(user.id, Clock.System)
        val repository2 = MemoryUserRepository(user.id, Clock.System)

        val compoundRepo = CompoundUserRepository(repository1, repository2)
    }) exercise {
        compoundRepo.save(user)
    } verify {
        repository2.getUser()?.data
            .assertIsEqualTo(user)
    }
}
