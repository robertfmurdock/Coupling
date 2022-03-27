package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.time.ExperimentalTime

@Suppress("unused")
@ExperimentalTime
class MemoryPlayerRepositoryTest : PlayerEmailRepositoryValidator<MemoryPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<TribeContext<MemoryPlayerRepository>>(sharedSetup = {
        object : TribeContext<MemoryPlayerRepository> {
            override val tribeId = stubTribeId()
            override val user = stubUser()
            override val clock = MagicClock()
            override val repository = MemoryPlayerRepository(user.email, clock)
        }
    })
}
