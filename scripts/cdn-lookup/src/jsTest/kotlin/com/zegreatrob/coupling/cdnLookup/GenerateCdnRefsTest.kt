package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class GenerateCdnRefsTest {

    @Test
    fun generateRefWorks() = asyncSetup(object {
        val lib = "react"
        val cdnLibs = listOf(lib)
    }) exercise {
        generateCdnRef(cdnLibs)
    } verify { result ->
        val version = getVersionForLibrary(lib)
        val expected = "https://esm.sh/react@$version"
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }

    @Test
    fun generateRefWorksForKotlin() = asyncSetup(object {
        val lib = "kotlin"
        val cdnLibs = listOf(lib)
    }) exercise {
        generateCdnRef(cdnLibs)
    } verify { result ->
        val version = getVersionForLibrary(lib)
        val expected = "https://esm.sh/kotlin@$version"
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }

    @Test
    fun generateRefWorksForAuth0() = asyncSetup(object {
        val lib = "@auth0/auth0-react"
        val cdnLibs = listOf(lib)
    }) exercise {
        generateCdnRef(cdnLibs)
    } verify { result ->
        val version = getVersionForLibrary(lib)
        val expected = "https://esm.sh/@auth0/auth0-react@$version"
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }
}
