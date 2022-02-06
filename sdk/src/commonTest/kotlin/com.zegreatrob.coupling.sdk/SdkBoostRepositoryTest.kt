package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.BoostRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.testmints.async.asyncTestTemplate

class SdkBoostRepositoryTest : BoostRepositoryValidator<Sdk, SharedContextData<Sdk>> {

    override val repositorySetup = asyncTestTemplate<SharedContextData<Sdk>>(sharedSetup = {
        val clock = MagicClock()
        val sdk = authorizedSdk()
        val user = sdk.getUser().data
        SharedContextData(sdk, clock, user)
    })

    override suspend fun buildRepository(user: User, clock: MagicClock) = altAuthorizedSdkDeferred.await()
}
