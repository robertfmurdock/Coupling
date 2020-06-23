package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeSharedContext
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryPlayerRepositoryTest : PlayerEmailRepositoryValidator<MemoryPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<TribeSharedContext<MemoryPlayerRepository>>(sharedSetup = {
        object : TribeSharedContext<MemoryPlayerRepository> {
            override val tribeId = stubTribeId()
            override val user = stubUser()
            override val clock = MagicClock()
            override val repository = MemoryPlayerRepository(user.email, clock)
        }
    })
}
