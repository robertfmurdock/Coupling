package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import korlibs.time.DateTime
import kotlin.test.Test

class SdkGlobalStatsTest {

    @Test
    fun canGetGlobalStats() = asyncSetup(object {
        val now = DateTime.now().year
    }) exercise {
        sdk().perform(
            graphQuery {
                globalStats(now)
            },
        )
    } verify { result ->
        result?.globalStats?.parties?.size
            .assertIsNotEqualTo(0)
    }
}
