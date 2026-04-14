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
        generateCdnRef(cdnLibs, lookupConfig)
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
        generateCdnRef(cdnLibs, lookupConfig)
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
        generateCdnRef(cdnLibs, lookupConfig)
    } verify { result ->
        val version = getVersionForLibrary(lib)
        val reactVersion = getVersionForLibrary("react")
        val reactDomVersion = getVersionForLibrary("react-dom")
        val expected =
            "https://esm.sh/@auth0/auth0-react@$version" +
                "?deps=react@$reactVersion,react-dom@$reactDomVersion&external=react,react-dom,react%2Fjsx-runtime"
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }

    @Test
    fun generateRefWorksForReactRouterDom() = asyncSetup(object {
        val lib = "react-router-dom"
        val cdnLibs = listOf(lib)
    }) exercise {
        generateCdnRef(cdnLibs, lookupConfig)
    } verify { result ->
        val version = getVersionForLibrary(lib)
        val reactVersion = getVersionForLibrary("react")
        val reactDomVersion = getVersionForLibrary("react-dom")
        val expected =
            "https://esm.sh/react-router-dom@$version" +
                "?deps=react@$reactVersion,react-dom@$reactDomVersion" +
                "&external=react,react-dom,react%2Fjsx-runtime,react-router,react-router%2Fdom,%40remix-run%2Frouter"
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }

    private companion object {
        val lookupConfig = CdnLookupConfig(
            profiles = mapOf(
                "reactProfile" to CdnLookupProfile(
                    deps = listOf("react", "react-dom"),
                    external = listOf("react", "react-dom", "react/jsx-runtime"),
                ),
                "reactRouterDomProfile" to CdnLookupProfile(
                    deps = listOf("react", "react-dom"),
                    external = listOf(
                        "react",
                        "react-dom",
                        "react/jsx-runtime",
                        "react-router",
                        "react-router/dom",
                        "@remix-run/router",
                    ),
                ),
            ),
            modules = mapOf(
                "@auth0/auth0-react" to CdnLookupModule(
                    global = "Auth0React",
                    inherits = "reactProfile",
                ),
                "react-router-dom" to CdnLookupModule(
                    global = "ReactRouterDom",
                    inherits = "reactRouterDomProfile",
                ),
            ),
        )
    }
}
