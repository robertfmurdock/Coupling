package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.MemoryTribeRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync2
import kotlin.test.Test

class CompoundTribeRepositoryTest : TribeRepositoryValidator {
    override suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit) {
        val stubUser = stubUser()

        val repository1 = MemoryTribeRepository(stubUser.email, clock)
        val repository2 = MemoryTribeRepository(stubUser.email, clock)

        val compoundRepo = CompoundTribeRepository(repository1, repository2)
        handler(compoundRepo, stubUser)
    }

    @Test
    fun saveWillWriteToSecondRepositoryAsWell() = setupAsync2(object {
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
    fun deleteWillWriteToSecondRepositoryAsWell() = setupAsync2(object {
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