package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.validation.LiveInfoRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotools.types.text.toNotBlankString
import kotlin.uuid.Uuid

@Suppress("unused")
class MemoryLiveInfoRepositoryTest : LiveInfoRepositoryValidator<MemoryLiveInfoRepository> {

    override val repositorySetup: TestTemplate<SharedContext<MemoryLiveInfoRepository>>
        get() = asyncTestTemplate(sharedSetup = {
            SharedContextData(
                MemoryLiveInfoRepository(),
                MagicClock(),
                UserDetails(
                    "${Uuid.random()}".toNotBlankString().getOrThrow(),
                    "${Uuid.random()}@mail.com".toNotBlankString().getOrThrow(),
                    emptySet(),
                    null,
                ),
            )
        })
}
