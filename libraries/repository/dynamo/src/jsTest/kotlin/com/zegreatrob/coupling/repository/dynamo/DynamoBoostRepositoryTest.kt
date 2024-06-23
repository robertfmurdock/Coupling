package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.validation.ExtendedBoostRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.testmints.async.asyncTestTemplate

class DynamoBoostRepositoryTest : ExtendedBoostRepositoryValidator<DynamoBoostRepository, SharedContextData<DynamoBoostRepository>> {

    override val repositorySetup = asyncTestTemplate(sharedSetup = {
        val user = stubUserDetails()
        val clock = MagicClock()
        SharedContextData(buildRepository(user, clock = clock), clock, user)
    })

    override suspend fun buildRepository(user: UserDetails, clock: MagicClock) = DynamoBoostRepository(user.id, clock)
}
