package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.BoostRepositoryValidator
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.testmints.async.asyncTestTemplate

class SdkBoostRepositoryTest : BoostRepositoryValidator<Sdk, SharedContextData<Sdk>> {

    override val repositorySetup = asyncTestTemplate<SharedContextData<Sdk>>(sharedSetup = {
        val clock = MagicClock()
        val sdk = authorizedKtorSdk()
        SharedContextData(sdk, clock, stubUser().copy(email = primaryAuthorizedUsername))
    })

    override suspend fun buildRepository(user: User, clock: MagicClock) = altAuthorizedSdkDeferred.await()
}