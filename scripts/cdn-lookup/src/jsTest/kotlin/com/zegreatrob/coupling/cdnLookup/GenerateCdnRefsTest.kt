package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test

class GenerateCdnRefsTest {

    @Test
    fun generateRefWorksWithoutConfiguration() = asyncSetup(object {
        val lib = "resolve-pkg"
    }) exercise {
        generateCdnRef(listOf(lib))
    } verify { result ->
        val version = getVersionForLibrary(lib)
        result.assertIsEqualTo(listOf(lib to "https://esm.sh/resolve-pkg@$version"))
    }

    @Test
    fun standaloneResultNamesGeneratedUrls() = asyncSetup(object {
        val lib = "resolve-pkg"
    }) exercise {
        generateCdnLookup(listOf(lib))
    } verify { result ->
        val version = getVersionForLibrary(lib)
        val expected = buildJsonObject {
            put("urls", buildJsonObject { put(lib, "https://esm.sh/resolve-pkg@$version") })
        }
        Json.parseToJsonElement(result.toJson()).assertIsEqualTo(expected)
    }

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
    fun generateRefWorksForResolvePkg() = asyncSetup(object {
        val lib = "resolve-pkg"
        val cdnLibs = listOf(lib)
    }) exercise {
        generateCdnRef(cdnLibs, lookupConfig)
    } verify { result ->
        val version = getVersionForLibrary(lib)
        val expected = "https://esm.sh/resolve-pkg@$version"
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
    fun generateRefWorksForReactRouter() = asyncSetup(object {
        val lib = "react-router"
        val cdnLibs = listOf(lib)
    }) exercise {
        generateCdnRef(cdnLibs, lookupConfig)
    } verify { result ->
        val version = getVersionForLibrary(lib)
        val reactVersion = getVersionForLibrary("react")
        val reactDomVersion = getVersionForLibrary("react-dom")
        val expected =
            "https://esm.sh/react-router@$version" +
                "?deps=react@$reactVersion,react-dom@$reactDomVersion" +
                "&external=react,react-dom,react%2Fjsx-runtime,react-router,react-router%2Fdom,%40remix-run%2Frouter"
        result.assertIsEqualTo(listOf(Pair(lib, expected)))
    }

    private companion object {
        val lookupConfig = CdnLookupConfig(
            profiles = mapOf(
                "react" to CdnLookupProfile(
                    dependencies = listOf("react", "react-dom"),
                    external = listOf("react", "react-dom", "react/jsx-runtime"),
                ),
                "reactRouterDom" to CdnLookupProfile(
                    dependencies = listOf("react", "react-dom"),
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
            imports = mapOf(
                "react" to CdnLookupImport(global = "React"),
                "react-dom" to CdnLookupImport(global = "ReactDOM"),
                "resolve-pkg" to CdnLookupImport(global = "ResolvePkg"),
                "@auth0/auth0-react" to CdnLookupImport(
                    global = "Auth0React",
                    profile = "react",
                ),
                "react-router" to CdnLookupImport(
                    global = "ReactRouterDom",
                    profile = "reactRouterDom",
                ),
            ),
        )
    }
}
