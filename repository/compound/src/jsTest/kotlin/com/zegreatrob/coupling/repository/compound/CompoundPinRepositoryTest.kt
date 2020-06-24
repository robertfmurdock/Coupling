package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.repository.validation.TribeContextData
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

class CompoundPinRepositoryTest : PinRepositoryValidator<CompoundPinRepository> {

    private val compoundRepositorySetup = asyncTestTemplate(sharedSetup = {
        object {
            val clock = MagicClock()
            val stubUser = stubUser()

            val repository1 = MemoryPinRepository(stubUser.email, clock)
            val repository2 = MemoryPinRepository(stubUser.email, clock)

            val compoundRepo = CompoundPinRepository(repository1, repository2)

            val tribeId = stubTribeId()
            val pin = stubPin()
        }
    })

    override val repositorySetup = compoundRepositorySetup
        .extend<TribeContext<CompoundPinRepository>>(sharedSetup = { parent ->
            with(parent) {
                TribeContextData(compoundRepo, tribeId, clock, stubUser)
            }
        })

    @Test
    fun saveWillWriteToSecondRepository() = compoundRepositorySetup() exercise {
        compoundRepo.save(tribeId.with(pin))
    } verify {
        repository2.getPins(tribeId).map { it.data.pin }.find { it._id == pin._id }
            .assertIsEqualTo(pin)
    }

    @Test
    fun deleteWillWriteToSecondRepository() = compoundRepositorySetup() exercise {
        compoundRepo.save(tribeId.with(pin))
        compoundRepo.deletePin(tribeId, pin._id!!)
    } verify {
        repository2.getPins(tribeId).map { it.data.pin }.find { it._id == pin._id }
            .assertIsEqualTo(null)
    }

}