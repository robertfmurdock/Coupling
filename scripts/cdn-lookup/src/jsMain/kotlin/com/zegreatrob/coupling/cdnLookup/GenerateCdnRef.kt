package com.zegreatrob.coupling.cdnLookup

import com.zegreatrob.coupling.cdnLookup.external.readpkgup.readPkgUp
import com.zegreatrob.coupling.cdnLookup.external.resolvepkg.resolvePkg
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlin.js.Json
import kotlin.js.json

// Resolve packages from the caller's working directory (the generated npm project),
// not from this script's build output directory.
val contextPath = js("process.cwd()").unsafeCast<String>()

suspend fun generateCdnRef(
    cdnLibs: List<String>,
    lookupConfig: CdnLookupConfig = CdnLookupConfig(),
): List<Pair<String, String>> = coroutineScope {
    validateLookupConfig(cdnLibs, lookupConfig)
    val versionLibs = (
        cdnLibs +
            lookupConfig.profiles.values.flatMap { it.dependencies } +
            lookupConfig.imports.values.flatMap { item -> item.query.dependencies }
        ).distinct()
    val versions = versionLibs
        .map { lib -> async { lib to getVersionForLibrary(lib) } }
        .awaitAll()
        .toMap()
    cdnLibs.map { lib -> async { lookupCdnUrl(lib, versions, lookupConfig) } }.awaitAll()
}

private suspend fun lookupCdnUrl(
    lib: String,
    versions: Map<String, String>,
    lookupConfig: CdnLookupConfig,
): Pair<String, String> {
    val version = versions.getValue(lib)

    val split = lib.indexOf("/")
    val (module, submodule) = if (lib.startsWith("@") || split < 0) lib to "" else lib.take(split) to lib.substring(split)
    val queryParameters = queryParametersFor(lib, versions, lookupConfig)

    return lib to "https://esm.sh/$module@$version$submodule$queryParameters"
}

private fun encodeQueryParamValue(value: String): String = js("encodeURIComponent")(value).unsafeCast<String>()

private fun queryParametersFor(
    lib: String,
    versions: Map<String, String>,
    lookupConfig: CdnLookupConfig,
): String {
    val item = lookupConfig.imports[lib] ?: return ""
    val inheritedProfile = item.profile?.let(lookupConfig.profiles::get)
    val profile = resolveImportQuery(item.query, inheritedProfile)
    val params = mutableListOf<String>()
    if (profile.dependencies.isNotEmpty()) {
        val deps = profile.dependencies
            .map { dependency -> "$dependency@${versions.getValue(dependency)}" }
            .joinToString(",")
        params.add("deps=$deps")
    }
    if (profile.external.isNotEmpty()) {
        val external = profile.external
            .joinToString(",") { dependency -> encodeQueryParamValue(dependency) }
        params.add("external=$external")
    }
    return if (params.isEmpty()) "" else "?${params.joinToString("&")}"
}

@Serializable
data class CdnLookupConfig(
    val profiles: Map<String, CdnLookupProfile> = emptyMap(),
    val imports: Map<String, CdnLookupImport> = emptyMap(),
)

@Serializable
data class CdnLookupProfile(
    val dependencies: List<String> = emptyList(),
    val external: List<String> = emptyList(),
)

@Serializable
data class CdnLookupImport(
    val global: String? = null,
    val profile: String? = null,
    val query: CdnLookupProfile = CdnLookupProfile(),
)

private fun resolveImportQuery(query: CdnLookupProfile, inheritedProfile: CdnLookupProfile?): CdnLookupProfile {
    val dependencies = if (query.dependencies.isNotEmpty()) {
        query.dependencies
    } else {
        inheritedProfile?.dependencies ?: emptyList()
    }
    val external = if (query.external.isNotEmpty()) {
        query.external
    } else {
        inheritedProfile?.external ?: emptyList()
    }
    return CdnLookupProfile(dependencies = dependencies, external = external)
}

private fun validateLookupConfig(cdnLibs: List<String>, lookupConfig: CdnLookupConfig) {
    val missingImports = cdnLibs.filterNot { lookupConfig.imports.containsKey(it) }
    if (missingImports.isNotEmpty()) {
        error("Missing import configuration for: ${missingImports.joinToString(", ")}")
    }

    lookupConfig.imports.forEach { (lib, import) ->
        import.profile?.let { profileName ->
            if (!lookupConfig.profiles.containsKey(profileName)) {
                error("Import '$lib' references unknown profile '$profileName'")
            }
        }
    }

    val availableDependencies = lookupConfig.imports.keys
    val configuredDependencies = (
        lookupConfig.profiles.values.flatMap { it.dependencies } +
            lookupConfig.imports.values.flatMap { it.query.dependencies }
        ).distinct()
    val unknownDependencies = configuredDependencies.filterNot { availableDependencies.contains(it) }
    if (unknownDependencies.isNotEmpty()) {
        error("Unknown dependencies in CDN settings: ${unknownDependencies.joinToString(", ")}")
    }
}

suspend fun getVersionForLibrary(lib: String): String {
    val declaredVersion = getDeclaredDependencyVersion(lib)
    if (declaredVersion != null) {
        return declaredVersion
    }

    val libPackage = resolvePkg(lib, json("cwd" to contextPath))
    val pkg = readPkgUp(json("cwd" to libPackage)).await()
    val resolvedVersion = pkg["pkg"].unsafeCast<Json>()["version"]?.unsafeCast<String?>()
    return resolvedVersion ?: error("Unable to determine version for '$lib'")
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
    val fileContents = js(
        """require("fs").readFileSync(require("path").join(process.cwd(), "package.json"), "utf8")""",
    ).unsafeCast<String>()
    return js("JSON.parse")(fileContents).unsafeCast<Json>()
}

private fun normalizeVersionConstraint(versionConstraint: String): String? {
    val match = Regex("""\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.-]+)?""").find(versionConstraint)
    return match?.value
}
