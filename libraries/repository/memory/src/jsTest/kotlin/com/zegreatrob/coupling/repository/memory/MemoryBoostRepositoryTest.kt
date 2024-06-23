package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.validation.ExtendedBoostRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.testmints.async.asyncTestTemplate

class MemoryBoostRepositoryTest : ExtendedBoostRepositoryValidator<MemoryBoostRepository, SharedContextData<MemoryBoostRepository>> {

    companion object {
        val recordBackend = SimpleRecordBackend<Boost>()
    }

    override val repositorySetup = asyncTestTemplate(sharedSetup = {
        val user = stubUserDetails()
        val clock = MagicClock()
        SharedContextData(buildRepository(user, clock = clock), clock, user)
    })

    override suspend fun buildRepository(user: UserDetails, clock: MagicClock) =
        MemoryBoostRepository(user.id, clock, recordBackend)
}
