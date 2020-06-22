package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.repository.memory.MemoryTribeRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class CompoundTribeRepositoryTest : TribeRepositoryValidator<CompoundTribeRepository> {

    override val repositorySetup: TestTemplate<SharedContext<CompoundTribeRepository>>
        get() = asyncTestTemplate(sharedSetup = {
            val stubUser = stubUser()
            val clock = MagicClock()

            val repository1 = MemoryTribeRepository(stubUser.email, clock)
            val repository2 = MemoryTribeRepository(stubUser.email, clock)

            val compoundRepo = CompoundTribeRepository(repository1, repository2)
            SharedContextData(compoundRepo, clock, stubUser)
        })

    @Test
    fun saveWillWriteToSecondRepositoryAsWell() = asyncSetup(object {
        val stubUser = stubUser()

        val repository1 = MemoryTribeRepository(stubUser.email, TimeProvider)
        val repository2 = MemoryTribeRepository(stubUser.email, TimeProvider)

        val compoundRepo = CompoundTribeRepository(repository1, repository2)

        val tribe = stubTribe()
    }) exercise {
        compoundRepo.save(tribe)
    } verify {
        repository2.getTribeRecord(tribe.id)
            ?.data
            .assertIsEqualTo(tribe)
    }

    @Test
    fun deleteWillWriteToSecondRepositoryAsWell() = asyncSetup(object {
        val stubUser = stubUser()

        val repository1 = MemoryTribeRepository(stubUser.email, TimeProvider)
        val repository2 = MemoryTribeRepository(stubUser.email, TimeProvider)

        val compoundRepo = CompoundTribeRepository(repository1, repository2)

        val tribe = stubTribe()
    }) exercise {
        compoundRepo.save(tribe)
        compoundRepo.delete(tribe.id)
    } verify {
        repository2.getTribeRecord(tribe.id)
            .assertIsEqualTo(null)
    }
}