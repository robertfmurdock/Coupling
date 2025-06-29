package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.repository.memory.MemoryPartyRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyRepositoryValidator
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test
import kotlin.time.Clock

class CompoundPartyRepositoryTest : PartyRepositoryValidator<CompoundPartyRepository> {

    override val repositorySetup: TestTemplate<SharedContext<CompoundPartyRepository>>
        get() = asyncTestTemplate(sharedSetup = {
            val stubUser = stubUserDetails()
            val clock = MagicClock()

            val repository1 = MemoryPartyRepository(stubUser.id, clock)
            val repository2 = MemoryPartyRepository(stubUser.id, clock)

            val compoundRepo = CompoundPartyRepository(repository1, repository2)
            SharedContextData(compoundRepo, clock, stubUser)
        })

    @Test
    fun saveWillWriteToSecondRepositoryAsWell() = asyncSetup(object {
        val stubUser = stubUserDetails()

        val repository1 = MemoryPartyRepository(stubUser.id, Clock.System)
        val repository2 = MemoryPartyRepository(stubUser.id, Clock.System)

        val compoundRepo = CompoundPartyRepository(repository1, repository2)

        val party = stubPartyDetails()
    }) exercise {
        compoundRepo.save(party)
    } verify {
        repository2.getDetails(party.id)
            ?.data
            .assertIsEqualTo(party)
    }

    @Test
    fun deleteWillWriteToSecondRepositoryAsWell() = asyncSetup(object {
        val stubUser = stubUserDetails()

        val repository1 = MemoryPartyRepository(stubUser.id, Clock.System)
        val repository2 = MemoryPartyRepository(stubUser.id, Clock.System)

        val compoundRepo = CompoundPartyRepository(repository1, repository2)

        val party = stubPartyDetails()
    }) exercise {
        compoundRepo.save(party)
        compoundRepo.deleteIt(party.id)
    } verify {
        repository2.getDetails(party.id)
            .assertIsEqualTo(null)
    }
}
