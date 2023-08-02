package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class SdkConfigTest {
    @Test
    fun canGetConfig() = asyncSetup(object {
    }) exercise {
        sdk().fire(graphQuery { config { discordClientId() } })
    } verify { result ->
        result?.config?.discordClientId
            .assertIsEqualTo("fake")
    }
}
