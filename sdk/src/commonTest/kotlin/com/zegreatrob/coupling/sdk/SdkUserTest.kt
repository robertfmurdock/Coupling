package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.CouplingQueryResult
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SdkUserTest {

    @Test
    fun canPerformUserQuery() = asyncSetup() exercise {
        sdk().perform(graphQuery { user() })
    } verify { result: CouplingQueryResult? ->
        result?.user.let {
            it?.email.assertIsEqualTo(primaryAuthorizedUsername)
            it?.id.assertIsNotEqualTo(null)
            it?.authorizedPartyIds.assertIsNotEqualTo(null)
        }
    }
}
