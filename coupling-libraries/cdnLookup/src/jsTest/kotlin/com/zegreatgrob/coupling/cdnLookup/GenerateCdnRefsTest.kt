package com.zegreatgrob.coupling.cdnLookup

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class GenerateCdnRefsTest {

    @Test
    fun generateRefWorks() = asyncSetup(object {
        val lib = "react"
        val cdnLibs = listOf(lib)
        val expected = "https://cdn.jsdelivr.net/npm/react@18.1.0/umd/react.production.min.js"
    }) exercise {
        generateCdnRef(cdnLibs)
    } verify { result ->
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }
}
