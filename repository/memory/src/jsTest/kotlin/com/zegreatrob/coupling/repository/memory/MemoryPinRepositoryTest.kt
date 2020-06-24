package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeSharedContext
import com.zegreatrob.coupling.repository.validation.TribeSharedContextData
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryPinRepositoryTest : PinRepositoryValidator<MemoryPinRepository> {

    override val repositorySetup = asyncTestTemplate<TribeSharedContext<MemoryPinRepository>>(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        TribeSharedContextData(MemoryPinRepository(user.email, clock), stubTribeId(), clock, user)
    })
}
