package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PartyContextData
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class CompoundPinRepositoryTest : PinRepositoryValidator<CompoundPinRepository> {

    private val compoundRepositorySetup = asyncTestTemplate(sharedSetup = {
        object {
            val clock = MagicClock()
            val stubUser = stubUserDetails()

            val repository1 = MemoryPinRepository(stubUser.email, clock)
            val repository2 = MemoryPinRepository(stubUser.email, clock)

            val compoundRepo = CompoundPinRepository(repository1, repository2)

            val partyId = stubPartyId()
            val pin = stubPin()
        }
    })

    override val repositorySetup = compoundRepositorySetup
        .extend<PartyContext<CompoundPinRepository>>(sharedSetup = { parent ->
            with(parent) {
                PartyContextData(compoundRepo, partyId, clock, stubUser)
            }
        })

    @Test
    fun saveWillWriteToSecondRepository() = compoundRepositorySetup() exercise {
        compoundRepo.save(partyId.with(pin))
    } verify {
        repository2.getPins(partyId).map { it.data.pin }.find { it.id == pin.id }
            .assertIsEqualTo(pin)
    }

    @Test
    fun deleteWillWriteToSecondRepository() = compoundRepositorySetup() exercise {
        compoundRepo.save(partyId.with(pin))
        compoundRepo.deletePin(partyId, pin.id!!)
    } verify {
        repository2.getPins(partyId).map { it.data.pin }.find { it.id == pin.id }
            .assertIsEqualTo(null)
    }
}
