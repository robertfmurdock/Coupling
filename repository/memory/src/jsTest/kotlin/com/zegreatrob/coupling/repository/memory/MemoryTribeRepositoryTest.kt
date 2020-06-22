package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryTribeRepositoryTest : TribeRepositoryValidator<MemoryTribeRepository> {
    override val repositorySetup = asyncTestTemplate<SharedContext<MemoryTribeRepository>>(sharedSetup = {
        val clock = MagicClock()
        val user = stubUser()
        val repo = MemoryTribeRepository(user.email, clock)
        SharedContextData(repo, clock, user)
    })
}
