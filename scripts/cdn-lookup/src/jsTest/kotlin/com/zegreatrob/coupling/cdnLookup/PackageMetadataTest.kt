package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import node.fs.MkdirSyncOptions
import node.fs.mkdirSync
import node.fs.mkdtempSync
import node.fs.writeFileSync
import node.os.tmpdir
import node.path.path
import kotlin.test.Test

class PackageMetadataTest {

    @Test
    fun resolvesFromExplicitWorkingDirectory() = asyncSetup(object {
        val fixture = packageFixture("fixture-package", "1.2.3")
    }) exercise {
        getVersionForLibrary("fixture-package", fixture)
    } verify { result ->
        result.assertIsEqualTo("1.2.3")
    }

    @Test
    fun reportsInstalledVersionInsteadOfDeclaredRange() = asyncSetup(object {
        val fixture = packageFixture("fixture-package", "2.3.4", declaredVersion = "^1.0.0")
    }) exercise {
        getVersionForLibrary("fixture-package", fixture)
    } verify { result ->
        result.assertIsEqualTo("2.3.4")
    }

    @Test
    fun resolvesSubpathThroughOwningPackage() = asyncSetup(object {
        val fixture = packageFixture("fixture-package", "3.4.5")
    }) exercise {
        getVersionForLibrary("fixture-package/subpath", fixture)
    } verify { result ->
        result.assertIsEqualTo("3.4.5")
    }

    @Test
    fun missingPackageNamesTheUninstalledImport() = asyncSetup(object {
        val fixture = packageFixture("installed-package", "1.0.0")
    }) exercise {
        runCatching { generateCdnLookup(listOf("missing-package"), workingDirectory = fixture) }
            .exceptionOrNull()
            ?.message
    } verify { result ->
        result.assertIsEqualTo("Unable to locate package metadata for 'missing-package'")
    }
}

private fun packageFixture(
    packageName: String,
    installedVersion: String,
    declaredVersion: String? = null,
): String {
    val fixture = mkdtempSync(path.join(tmpdir(), "cdn-lookup-"))
    val packageDirectory = path.join(fixture, "node_modules", packageName)
    val options = js("({ recursive: true })").unsafeCast<MkdirSyncOptions>()
    mkdirSync(packageDirectory, options)
    val dependencies = declaredVersion?.let { ""","dependencies":{"$packageName":"$it"}""" }.orEmpty()
    writeFileSync(path.join(fixture, "package.json"), """{"private":true$dependencies}""")
    writeFileSync(
        path.join(packageDirectory, "package.json"),
        """{"name":"$packageName","version":"$installedVersion"}""",
    )
    return fixture
}
