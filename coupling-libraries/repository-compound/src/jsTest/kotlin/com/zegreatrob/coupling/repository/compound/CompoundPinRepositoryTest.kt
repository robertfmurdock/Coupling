package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PartyContextData
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate

import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class CompoundPinRepositoryTest : PinRepositoryValidator<CompoundPinRepository> {

    private val compoundRepositorySetup = asyncTestTemplate(sharedSetup = {
        object {
            val clock = MagicClock()
            val stubUser = stubUser()

            val repository1 = MemoryPinRepository(stubUser.email, clock)
            val repository2 = MemoryPinRepository(stubUser.email, clock)

            val compoundRepo = CompoundPinRepository(repository1, repository2)

            val tribeId = stubPartyId()
            val pin = stubPin()
        }
    })

    override val repositorySetup = compoundRepositorySetup
        .extend<PartyContext<CompoundPinRepository>>(sharedSetup = { parent ->
            with(parent) {
                PartyContextData(compoundRepo, tribeId, clock, stubUser)
            }
        })

    @Test
    fun saveWillWriteToSecondRepository() = compoundRepositorySetup() exercise {
        compoundRepo.save(tribeId.with(pin))
    } verify {
        repository2.getPins(tribeId).map { it.data.pin }.find { it.id == pin.id }
            .assertIsEqualTo(pin)
    }

    @Test
    fun deleteWillWriteToSecondRepository() = compoundRepositorySetup() exercise {
        compoundRepo.save(tribeId.with(pin))
        compoundRepo.deletePin(tribeId, pin.id!!)
    } verify {
        repository2.getPins(tribeId).map { it.data.pin }.find { it.id == pin.id }
            .assertIsEqualTo(null)
    }

}