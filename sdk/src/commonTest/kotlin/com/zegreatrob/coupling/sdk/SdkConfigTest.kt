package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.ConfigQuery
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class SdkConfigTest {
    @Test
    fun canGetConfig() = asyncSetup(object {
    }) exercise {
        sdk().fire(GqlQuery(ConfigQuery()))
    } verify { result ->
        result?.config?.discordClientId
            .assertIsEqualTo("fake")
    }
}
