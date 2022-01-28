package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.BoostRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate

class MemoryBoostRepositoryTest :
    BoostRepositoryValidator<MemoryBoostRepository, SharedContextData<MemoryBoostRepository>> {

    companion object {
        val recordBackend = SimpleRecordBackend<Boost>()
    }

    override val boostSetup = asyncTestTemplate(sharedSetup = {
        val user = stubUser()
        val clock = MagicClock()
        SharedContextData(buildRepository(user, clock = clock), clock, user)
    })

    override fun buildRepository(user: User, clock: MagicClock) = MemoryBoostRepository(user.id, clock, recordBackend)

}
