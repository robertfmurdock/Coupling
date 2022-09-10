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

    @Test
    fun generateRefWorksForKotlin() = asyncSetup(object {
        val lib = "kotlin"
        val cdnLibs = listOf(lib)
        val expected = "https://cdn.jsdelivr.net/npm/kotlin@1.7.20-RC/kotlin.min.js"
    }) exercise {
        generateCdnRef(cdnLibs)
    } verify { result ->
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }

    @Test
    fun generateRefWorksForAuth0() = asyncSetup(object {
        val lib = "@auth0/auth0-react"
        val cdnLibs = listOf(lib)
        val expected = "https://cdn.jsdelivr.net/npm/@auth0/auth0-react@1.10.2/dist/auth0-react.min.js"
    }) exercise {
        generateCdnRef(cdnLibs)
    } verify { result ->
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }
}
