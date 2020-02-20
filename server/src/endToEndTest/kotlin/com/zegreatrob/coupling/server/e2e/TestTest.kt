package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class TestTest {

    @Test
    fun placeholderForNow() = setup(object {
    }) exercise {
        "nah"
    } verify { result ->
        result.assertIsEqualTo("nah")
    }

}