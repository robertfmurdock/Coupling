package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class SdkUserRepositoryTest {

    val repositorySetup = asyncTestTemplate<SharedContextData<Sdk>>(sharedSetup = {
        val clock = MagicClock()
        val sdk = authorizedSdk()
        SharedContextData(sdk, clock, stubUser().copy(email = primaryAuthorizedUsername))
    })

    @Test
    fun canPerformUserQuery() = repositorySetup {
    } exercise {
        repository.perform(UserQuery())
    } verify { result ->
        result.let {
            it?.email.assertIsEqualTo(primaryAuthorizedUsername)
            it?.id.assertIsNotEqualTo(null)
            it?.authorizedPartyIds.assertIsNotEqualTo(null)
        }
    }

}
