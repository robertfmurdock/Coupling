package com.zegreatrob.coupling.sdk
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SdkUserTest {

    @Test
    fun canPerformUserQuery() = asyncSetup() exercise {
        authorizedSdk().perform(UserQuery())
    } verify { result ->
        result.let {
            it?.email.assertIsEqualTo(primaryAuthorizedUsername)
            it?.id.assertIsNotEqualTo(null)
            it?.authorizedPartyIds.assertIsNotEqualTo(null)
        }
    }
}
