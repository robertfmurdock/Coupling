package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.coupling.cdnLookup.external.readpkgup.readPackageUp
import com.zegreatrob.coupling.cdnLookup.external.resolvepkg.resolvePkg
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

internal class PackageMetadata(private val workingDirectory: String) {
    private val packageJsonCache = mutableMapOf<String, Json?>()

    suspend fun versionForLibrary(lib: String): String {
        val pkg = packageJsonForPackage(packageNameForImport(lib))
            ?: error("Unable to locate package metadata for '$lib'")
        return pkg["version"]?.unsafeCast<String?>()
            ?: error("Unable to determine version for '$lib'")
    }

    suspend fun packageJsonForPackage(packageName: String): Json? {
        packageJsonCache[packageName]?.let { return it }

        val libPackage = resolvePkg(packageName, json("cwd" to workingDirectory)) ?: return null
        val pkg = readPackageUp(json("cwd" to libPackage)).await()
        return pkg?.get("packageJson")
            ?.unsafeCast<Json?>()
            .also { packageJsonCache[packageName] = it }
    }
}

suspend fun getVersionForLibrary(
    lib: String,
    workingDirectory: String = currentWorkingDirectory(),
): String = PackageMetadata(workingDirectory).versionForLibrary(lib)

internal fun currentWorkingDirectory(): String = js("process.cwd()").unsafeCast<String>()
