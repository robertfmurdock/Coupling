package com.zegreatrob.coupling.cdnLookup

import com.github.ajalt.clikt.command.test
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import node.fs.mkdtempSync
import node.fs.writeFileSync
import node.os.tmpdir
import node.path.path
import kotlin.test.Test

class CdnLookupCommandTest {

    @Test
    fun helpDescribesPublicCommand() = asyncSetup(object {
    }) exercise {
        CdnLookupCommand().test("--help")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        result.output.assertIsEqualTo(
            """
            Usage: cdn-lookup [<options>] [<libraries>]...

              Generate pinned CDN URLs for installed npm packages.

            Options:
              --config=<text>    Read lookup configuration from this JSON file.
              --provider=<text>  CDN provider to use.
              --output=<text>    Output format to write to stdout.
              --version          Show the version and exit
              -h, --help         Show this message and exit

            Arguments:
              <libraries>  Package imports to resolve.

            """.trimIndent(),
        )
    }

    @Test
    fun versionIsAvailable() = asyncSetup(object {
    }) exercise {
        CdnLookupCommand().test("--version")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        result.output.trim()
            .assertIsEqualTo("cdn-lookup version 0.0.0")
    }

    @Test
    fun configFileCanCustomizeLookup() = asyncSetup(object {
        val lib = "resolve-pkg"
        val fixture = mkdtempSync(path.join(tmpdir(), "cdn-lookup-command-"))
        val configFile = path.join(fixture, ".cdn-lookup.json")
    }) {
        writeFileSync(
            configFile,
            """
            {
              "imports": {
                "$lib": {
                  "query": {
                    "external": ["react"]
                  }
                }
              }
            }
            """.trimIndent(),
        )
    } exercise {
        getVersionForLibrary(lib) to CdnLookupCommand().test("--config $configFile $lib")
    } verify { (version, result) ->
        result.statusCode.assertIsEqualTo(0, result.output)
        val expected = buildJsonObject {
            put("urls", buildJsonObject { put(lib, "https://esm.sh/$lib@$version?external=react") })
        }
        Json.parseToJsonElement(result.output)
            .assertIsEqualTo(expected)
    }

    @Test
    fun esmShProviderCanBeSelectedExplicitly() = asyncSetup(object {
        val lib = "resolve-pkg"
    }) exercise {
        getVersionForLibrary(lib) to CdnLookupCommand().test("--provider esm.sh $lib")
    } verify { (version, result) ->
        result.statusCode.assertIsEqualTo(0, result.output)
        val expected = buildJsonObject {
            put("urls", buildJsonObject { put(lib, "https://esm.sh/$lib@$version") })
        }
        Json.parseToJsonElement(result.output)
            .assertIsEqualTo(expected)
    }

    @Test
    fun jsonOutputCanBeSelectedExplicitly() = asyncSetup(object {
        val lib = "resolve-pkg"
    }) exercise {
        getVersionForLibrary(lib) to CdnLookupCommand().test("--output json $lib")
    } verify { (version, result) ->
        result.statusCode.assertIsEqualTo(0, result.output)
        val expected = buildJsonObject {
            put("urls", buildJsonObject { put(lib, "https://esm.sh/$lib@$version") })
        }
        Json.parseToJsonElement(result.output)
            .assertIsEqualTo(expected)
    }

    @Test
    fun unsupportedProviderReportsError() = asyncSetup(object {
    }) exercise {
        CdnLookupCommand().test("--provider unpkg resolve-pkg")
    } verify { result ->
        result.statusCode.assertIsEqualTo(1, result.output)
        result.stdout.assertIsEqualTo("")
        result.stderr.assertIsEqualTo("Unsupported CDN provider 'unpkg'\n")
    }

    @Test
    fun scopedPackageNamesAreTreatedAsPackageImports() = asyncSetup(object {
        val lib = "@auth0/auth0-react"
    }) exercise {
        getVersionForLibrary(lib) to CdnLookupCommand().test(lib)
    } verify { (version, result) ->
        result.statusCode.assertIsEqualTo(0, result.output)
        val expected = buildJsonObject {
            put("urls", buildJsonObject { put(lib, "https://esm.sh/$lib@$version") })
        }
        Json.parseToJsonElement(result.output)
            .assertIsEqualTo(expected)
    }
}
