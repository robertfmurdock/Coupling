package com.zegreatgrob.coupling.cdnLookup

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class GenerateCdnRefsTest {

    @Test
    fun sdf() = asyncSetup(object {
        val cdnLibs = listOf("react")
        val expected = "https://cdnjs.cloudflare.com/ajax/libs/react/18.1.0/umd/react.production.min.js"
    }) exercise {
        generateCdnRef(cdnLibs)
    } verify { result ->
        result.assertIsEqualTo(listOf(expected))
    }
}
