package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryTribeRepositoryTest : TribeRepositoryValidator<MemoryPartyRepository> {
    override val repositorySetup = asyncTestTemplate<SharedContext<MemoryPartyRepository>>(sharedSetup = {
        val clock = MagicClock()
        val user = stubUser()
        val repo = MemoryPartyRepository(user.email, clock)
        SharedContextData(repo, clock, user)
    })
}
