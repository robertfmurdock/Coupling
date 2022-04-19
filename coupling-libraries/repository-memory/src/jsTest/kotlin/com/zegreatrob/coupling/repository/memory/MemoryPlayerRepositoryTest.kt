package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.time.ExperimentalTime

@Suppress("unused")
@ExperimentalTime
class MemoryPlayerRepositoryTest : PlayerEmailRepositoryValidator<MemoryPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<PartyContext<MemoryPlayerRepository>>(sharedSetup = {
        object : PartyContext<MemoryPlayerRepository> {
            override val partyId = stubPartyId()
            override val user = stubUser()
            override val clock = MagicClock()
            override val repository = MemoryPlayerRepository(user.email, clock)
        }
    })
}
