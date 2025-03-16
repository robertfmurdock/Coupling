package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlin.test.Test

class SdkUserTest {

    @Test
    fun canPerformUserQuery() = asyncSetup() exercise {
        sdk().fire(graphQuery { user { details() } })
    } verify { result: CouplingQueryResult? ->
        result?.user?.details.let {
            it?.email.toString().assertIsEqualTo(PRIMARY_AUTHORIZED_USER_NAME)
            it?.id.assertIsNotEqualTo(null)
            it?.authorizedPartyIds.assertIsNotEqualTo(null)
        }
    }
}
