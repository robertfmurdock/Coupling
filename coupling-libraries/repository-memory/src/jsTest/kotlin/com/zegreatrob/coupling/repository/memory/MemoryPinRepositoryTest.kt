package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PartyContextData
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.time.ExperimentalTime

@Suppress("unused")
@ExperimentalTime
class MemoryPinRepositoryTest : PinRepositoryValidator<MemoryPinRepository> {

    override val repositorySetup = asyncTestTemplate<PartyContext<MemoryPinRepository>>(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        PartyContextData(MemoryPinRepository(user.email, clock), stubPartyId(), clock, user)
    })
}
