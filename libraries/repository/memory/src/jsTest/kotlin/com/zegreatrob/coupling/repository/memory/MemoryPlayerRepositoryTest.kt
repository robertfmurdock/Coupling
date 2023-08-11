package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryPlayerRepositoryTest : PlayerEmailRepositoryValidator<MemoryPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<PartyContext<MemoryPlayerRepository>>(sharedSetup = {
        object : PartyContext<MemoryPlayerRepository> {
            override val partyId = stubPartyId()
            override val user = stubUserDetails()
            override val clock = MagicClock()
            override val repository = MemoryPlayerRepository(user.email, clock)
        }
    })
}
