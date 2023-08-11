package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyRepositoryValidator
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryPartyRepositoryTest : PartyRepositoryValidator<MemoryPartyRepository> {
    override val repositorySetup = asyncTestTemplate<SharedContext<MemoryPartyRepository>>(sharedSetup = {
        val clock = MagicClock()
        val user = stubUserDetails()
        val repo = MemoryPartyRepository(user.email, clock)
        SharedContextData(repo, clock, user)
    })
}
