package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.coupling.cdnLookup.external.readpkgup.readPackageUp
import com.zegreatrob.coupling.cdnLookup.external.resolvepkg.resolvePkg
import kotlinx.coroutines.await
import node.buffer.BufferEncoding
import node.buffer.utf8
import node.fs.readFileSync
import node.path.path
import kotlin.js.Json
import kotlin.js.json

// Resolve packages from the caller's working directory (the generated npm project),
// not from this script's build output directory.
private val contextPath = js("process.cwd()").unsafeCast<String>()
private val packageJsonCache = mutableMapOf<String, Json?>()
private var workingDirectoryPackageJson: Json? = null
private val semverConstraintPattern = Regex("""\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.-]+)?""")

suspend fun getVersionForLibrary(lib: String): String {
    getDeclaredDependencyVersion(lib)?.let { return it }

    val packageName = packageNameForImport(lib)
    val pkg = getPackageJsonForPackage(packageName)
        ?: error("Unable to locate package metadata for '$lib'")
    val resolvedVersion = pkg["version"]?.unsafeCast<String?>()
    return resolvedVersion ?: error("Unable to determine version for '$lib'")
}

internal suspend fun getPackageJsonForPackage(packageName: String): Json? {
    packageJsonCache[packageName]?.let { return it }

    val libPackage = resolvePkg(packageName, json("cwd" to contextPath)) ?: return null
    val pkg = readPackageUp(json("cwd" to libPackage)).await()
    val parsed = pkg?.get("packageJson")?.unsafeCast<Json?>()
    packageJsonCache[packageName] = parsed
    return parsed
}

private fun getDeclaredDependencyVersion(lib: String): String? {
    val pkg = readWorkingDirectoryPackageJson()
    val candidates = listOf("dependencies", "devDependencies", "peerDependencies", "optionalDependencies")
        .mapNotNull { sectionName ->
            val section = pkg[sectionName]?.unsafeCast<Json?>() ?: return@mapNotNull null
            section[lib]?.unsafeCast<String?>()
        }
    return candidates.firstNotNullOfOrNull(::normalizeVersionConstraint)
}

private fun readWorkingDirectoryPackageJson(): Json {
    workingDirectoryPackageJson?.let { return it }

    val fileContents = readFileSync(path.join(contextPath, "package.json"), BufferEncoding.utf8)
    return js("JSON.parse")(fileContents).unsafeCast<Json>().also { parsed ->
        workingDirectoryPackageJson = parsed
    }
}

private fun normalizeVersionConstraint(versionConstraint: String): String? = semverConstraintPattern.find(versionConstraint)?.value
