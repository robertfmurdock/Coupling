package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PartyContextData
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryPinRepositoryTest : PinRepositoryValidator<MemoryPinRepository> {

    override val repositorySetup = asyncTestTemplate<PartyContext<MemoryPinRepository>>(sharedSetup = {
        val user = stubUserDetails()
        val clock = MagicClock()
        PartyContextData(MemoryPinRepository(user.id, clock), stubPartyId(), clock, user)
    })
}
