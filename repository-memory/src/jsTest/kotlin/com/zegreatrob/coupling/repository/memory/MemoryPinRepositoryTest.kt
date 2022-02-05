package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.repository.validation.TribeContextData
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryPinRepositoryTest : PinRepositoryValidator<MemoryPinRepository, TribeContext<MemoryPinRepository>> {

    override val repositorySetup = asyncTestTemplate<TribeContext<MemoryPinRepository>>(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        TribeContextData(MemoryPinRepository(user.email, clock), stubTribeId(), clock, user)
    })
}
