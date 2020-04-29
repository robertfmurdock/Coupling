package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
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
    fun saveWillWriteToSecondRepository() = asyncSetup(object {
        val stubUser = stubUser()

        val repository1 = MemoryPinRepository(stubUser.email, TimeProvider)
        val repository2 = MemoryPinRepository(stubUser.email, TimeProvider)

        val compoundRepo = CompoundPinRepository(repository1, repository2)

        val tribeId = stubTribeId()
        val pin = stubPin()
    }) exercise {
        compoundRepo.save(tribeId.with(pin))
    } verify {
        repository2.getPins(tribeId).map { it.data.pin }.find { it._id == pin._id }
            .assertIsEqualTo(pin)
    }

    @Test
    fun deleteWillWriteToSecondRepository() = asyncSetup(object {
        val stubUser = stubUser()

        val repository1 = MemoryPinRepository(stubUser.email, TimeProvider)
        val repository2 = MemoryPinRepository(stubUser.email, TimeProvider)

        val compoundRepo = CompoundPinRepository(repository1, repository2)

        val tribeId = stubTribeId()
        val pin = stubPin()
    }) exercise {
        compoundRepo.save(tribeId.with(pin))
        compoundRepo.deletePin(tribeId, pin._id!!)
    } verify {
        repository2.getPins(tribeId).map { it.data.pin }.find { it._id == pin._id }
            .assertIsEqualTo(null)
    }

}