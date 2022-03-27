package com.zegreatrob.coupling.repository.dynamo

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.LiveInfoRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.time.ExperimentalTime

@Suppress("unused")
@ExperimentalTime
class DynamoLiveInfoRepositoryTest : LiveInfoRepositoryValidator<DynamoLiveInfoRepository> {
    override val repositorySetup = asyncTestTemplate<SharedContext<DynamoLiveInfoRepository>>(sharedSetup = {
        val clock = MagicClock()
        val userId = "${uuid4()}"
        val user = User(userId, "${uuid4()}", emptySet())
        val repository = DynamoLiveInfoRepository(userId, clock)
        SharedContextData(repository, clock, user)
    })
}
