package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.MemoryTribeRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
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
    fun saveWillWriteToSecondRepositoryAsWell() = testAsync {
        setupAsync(object {
            val stubUser = stubUser()

            val repository1 = MemoryTribeRepository(stubUser.email, TimeProvider)
            val repository2 = MemoryTribeRepository(stubUser.email, TimeProvider)

            val compoundRepo = CompoundTribeRepository(repository1, repository2)

            val tribe = stubTribe()
        }) exerciseAsync {
            compoundRepo.save(tribe)
        } verifyAsync {
            repository2.getTribeRecord(tribe.id)
                ?.data
                .assertIsEqualTo(tribe)
        }
    }

    @Test
    fun deleteWillWriteToSecondRepositoryAsWell() = testAsync {
        setupAsync(object {
            val stubUser = stubUser()

            val repository1 = MemoryTribeRepository(stubUser.email, TimeProvider)
            val repository2 = MemoryTribeRepository(stubUser.email, TimeProvider)

            val compoundRepo = CompoundTribeRepository(repository1, repository2)

            val tribe = stubTribe()
        }) exerciseAsync {
            compoundRepo.save(tribe)
            compoundRepo.delete(tribe.id)
        } verifyAsync {
            repository2.getTribeRecord(tribe.id)
                .assertIsEqualTo(null)
        }
    }
}