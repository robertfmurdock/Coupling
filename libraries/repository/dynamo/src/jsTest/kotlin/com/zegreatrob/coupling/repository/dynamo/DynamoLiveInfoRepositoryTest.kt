package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.validation.LiveInfoRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotools.types.text.toNotBlankString

@Suppress("unused")
class DynamoLiveInfoRepositoryTest : LiveInfoRepositoryValidator<DynamoLiveInfoRepository> {
    override val repositorySetup = asyncTestTemplate<SharedContext<DynamoLiveInfoRepository>>(sharedSetup = {
        val clock = MagicClock()
        val userId = UserId.new()
        val user = UserDetails(userId, uuidString().toNotBlankString().getOrThrow(), emptySet(), uuidString(), null)
        val repository = DynamoLiveInfoRepository(userId, clock)
        SharedContextData(repository, clock, user)
    })
}
