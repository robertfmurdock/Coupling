package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.pin.with
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubPin
import stubTribeId
import stubUser
import kotlin.test.Test

class CompoundPinRepositoryTest : PinRepositoryValidator {

    override suspend fun withRepository(clock: TimeProvider, handler: suspend (PinRepository, TribeId, User) -> Unit) {
        val stubUser = stubUser()

        val repository1 = MemoryPinRepository(stubUser.email, clock)
        val repository2 = MemoryPinRepository(stubUser.email, clock)

        val compoundRepo = CompoundPinRepository(repository1, repository2)
        handler(compoundRepo, stubTribeId(), stubUser)
    }

    @Test
    fun saveWillWriteToSecondRepository() = testAsync {
        setupAsync(object {
            val stubUser = stubUser()

            val repository1 = MemoryPinRepository(stubUser.email, TimeProvider)
            val repository2 = MemoryPinRepository(stubUser.email, TimeProvider)

            val compoundRepo = CompoundPinRepository(repository1, repository2)

            val tribeId = stubTribeId()
            val pin = stubPin()
        }) exerciseAsync {
            compoundRepo.save(pin.with(tribeId))
        } verifyAsync {
            repository2.getPins(tribeId).map { it.data.pin }.find { it._id == pin._id }
                .assertIsEqualTo(pin)
        }
    }

    @Test
    fun deleteWillWriteToSecondRepository() = testAsync {
        setupAsync(object {
            val stubUser = stubUser()

            val repository1 = MemoryPinRepository(stubUser.email, TimeProvider)
            val repository2 = MemoryPinRepository(stubUser.email, TimeProvider)

            val compoundRepo = CompoundPinRepository(repository1, repository2)

            val tribeId = stubTribeId()
            val pin = stubPin()
        }) exerciseAsync {
            compoundRepo.save(pin.with(tribeId))
            compoundRepo.deletePin(tribeId, pin._id!!)
        } verifyAsync {
            repository2.getPins(tribeId).map { it.data.pin }.find { it._id == pin._id }
                .assertIsEqualTo(null)
        }
    }

}