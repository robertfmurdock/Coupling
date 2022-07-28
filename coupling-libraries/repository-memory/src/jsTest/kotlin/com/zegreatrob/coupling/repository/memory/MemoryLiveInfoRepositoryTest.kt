package com.zegreatrob.coupling.repository.memory

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.LiveInfoRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate

@Suppress("unused")
class MemoryLiveInfoRepositoryTest : LiveInfoRepositoryValidator<MemoryLiveInfoRepository> {

    override val repositorySetup: TestTemplate<SharedContext<MemoryLiveInfoRepository>>
        get() = asyncTestTemplate(sharedSetup = {
            SharedContextData(
                MemoryLiveInfoRepository(),
                MagicClock(),
                User("${uuid4()}", "${uuid4()}@mail.com", emptySet())
            )
        })
}
