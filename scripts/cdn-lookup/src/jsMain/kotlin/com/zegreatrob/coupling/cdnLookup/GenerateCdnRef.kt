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
private val packageJsonCache = mutableMapOf<String, Json?>()

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

private suspend fun queryParametersFor(
    lib: String,
    versions: Map<String, String>,
    lookupConfig: CdnLookupConfig,
): String {
    val item = lookupConfig.imports[lib] ?: return ""
    val singletonDependencies = configuredSingletonDependencies(lookupConfig)
    val dependencyGroups = configuredDependencyGroups(lookupConfig)
    val inheritedProfile = item.profile?.let(lookupConfig.profiles::get)
    val profile = resolveImportQuery(item.query, inheritedProfile)
    val effectiveProfile = if (profile.dependencies.isNotEmpty() || profile.external.isNotEmpty()) {
        profile
    } else {
        deriveImportQueryFromPackage(
            lib,
            singletonDependencies,
            dependencyGroups,
            lookupConfig.imports.keys,
        )
    }
    val params = mutableListOf<String>()
    if (effectiveProfile.dependencies.isNotEmpty()) {
        val deps = effectiveProfile.dependencies
            .map { dependency -> "$dependency@${versions.getValue(dependency)}" }
            .joinToString(",")
        params.add("deps=$deps")
    }
    if (effectiveProfile.external.isNotEmpty()) {
        val external = effectiveProfile.external
            .joinToString(",") { dependency -> encodeQueryParamValue(dependency) }
        params.add("external=$external")
    }
    return if (params.isEmpty()) "" else "?${params.joinToString("&")}"
}

private suspend fun deriveImportQueryFromPackage(
    lib: String,
    singletonDependencies: Set<String>,
    dependencyGroups: List<Set<String>>,
    availableImports: Set<String>,
): CdnLookupProfile {
    val packageName = packageNameForImport(lib)
    val derivedDependencies = collectPeerDependenciesFromPackageGraph(packageName, availableImports)
        .filter(singletonDependencies::contains)
        .let { expandDependencyGroups(it.toSet(), dependencyGroups, availableImports) }
    if (derivedDependencies.isEmpty()) {
        return CdnLookupProfile()
    }
    val derivedExternal = buildList {
        addAll(derivedDependencies)
        derivedDependencies.forEach { dependency ->
            val jsxRuntimeImport = "$dependency/jsx-runtime"
            if (availableImports.contains(jsxRuntimeImport)) {
                add(jsxRuntimeImport)
            }
        }
    }.distinct()
    return CdnLookupProfile(
        dependencies = derivedDependencies,
        external = derivedExternal,
    )
}

private suspend fun collectPeerDependenciesFromPackageGraph(
    packageName: String,
    availableImports: Set<String>,
): List<String> {
    val queue = ArrayDeque<Pair<String, Int>>()
    val visitedDepth = mutableMapOf<String, Int>()
    val peers = linkedSetOf<String>()
    var minimumPeerDepth: Int? = null
    queue.add(packageName to 0)

    while (queue.isNotEmpty() && visitedDepth.size < 128) {
        val (current, depth) = queue.removeFirst()
        val priorDepth = visitedDepth[current]
        if (priorDepth != null && priorDepth <= depth) continue
        visitedDepth[current] = depth

        if (minimumPeerDepth != null && depth > minimumPeerDepth) {
            continue
        }
        val pkg = getPackageJsonForPackage(current) ?: continue

        val peerDependencies = pkg["peerDependencies"]?.unsafeCast<Json?>()
        if (peerDependencies != null) {
            val matchingPeers = objectKeys(peerDependencies)
                .filter(availableImports::contains)
            if (matchingPeers.isNotEmpty()) {
                when {
                    minimumPeerDepth == null || depth < minimumPeerDepth -> {
                        minimumPeerDepth = depth
                        peers.clear()
                        peers.addAll(matchingPeers)
                    }

                    depth == minimumPeerDepth -> {
                        peers.addAll(matchingPeers)
                    }
                }
            }
        }

        if (minimumPeerDepth != null && depth >= minimumPeerDepth) {
            continue
        }
        val dependencies = pkg["dependencies"]?.unsafeCast<Json?>()
        if (dependencies != null) {
            objectKeys(dependencies).forEach { dependency ->
                queue.addLast(dependency to depth + 1)
            }
        }
    }

    return peers.toList().distinct()
}

private fun configuredSingletonDependencies(lookupConfig: CdnLookupConfig): Set<String> = (
    lookupConfig.profiles.values.flatMap { it.dependencies } +
        lookupConfig.imports.values.flatMap { it.query.dependencies }
    ).toSet()

private fun configuredDependencyGroups(lookupConfig: CdnLookupConfig): List<Set<String>> = buildList {
    addAll(lookupConfig.profiles.values.map { it.dependencies.toSet() })
    addAll(lookupConfig.imports.values.map { it.query.dependencies.toSet() })
}.filter { it.isNotEmpty() }

private fun expandDependencyGroups(
    derivedDependencies: Set<String>,
    dependencyGroups: List<Set<String>>,
    availableImports: Set<String>,
): List<String> {
    val expanded = linkedSetOf<String>()
    expanded.addAll(derivedDependencies)
    dependencyGroups.forEach { group ->
        if (group.any(derivedDependencies::contains)) {
            expanded.addAll(group.filter(availableImports::contains))
        }
    }
    return expanded.toList()
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

    val pkg = getPackageJsonForLibrary(lib)
        ?: error("Unable to locate package metadata for '$lib'")
    val resolvedVersion = pkg["version"]?.unsafeCast<String?>()
    return resolvedVersion ?: error("Unable to determine version for '$lib'")
}

private suspend fun getPackageJsonForLibrary(lib: String): Json? {
    val packageName = packageNameForImport(lib)
    return getPackageJsonForPackage(packageName)
}

private suspend fun getPackageJsonForPackage(packageName: String): Json? {
    if (packageJsonCache.containsKey(packageName)) {
        return packageJsonCache[packageName]
    }
    val libPackage = resolvePkg(packageName, json("cwd" to contextPath))
    val pkg = readPkgUp(json("cwd" to libPackage)).await()
    val parsed = pkg["pkg"]?.unsafeCast<Json?>()
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
    val fileContents = js(
        """require("fs").readFileSync(require("path").join(process.cwd(), "package.json"), "utf8")""",
    ).unsafeCast<String>()
    return js("JSON.parse")(fileContents).unsafeCast<Json>()
}

private fun normalizeVersionConstraint(versionConstraint: String): String? {
    val match = Regex("""\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.-]+)?""").find(versionConstraint)
    return match?.value
}

private fun objectKeys(json: Json): List<String> = js("Object.keys")(json).unsafeCast<Array<String>>().toList()

private fun packageNameForImport(importName: String): String {
    val segments = importName.split("/")
    return if (importName.startsWith("@")) {
        segments.take(2).joinToString("/")
    } else {
        segments.first()
    }
}
