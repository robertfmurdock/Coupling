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

val contextPath = js("__dirname").unsafeCast<String>()

suspend fun generateCdnRef(
    cdnLibs: List<String>,
    lookupConfig: CdnLookupConfig = CdnLookupConfig(),
): List<Pair<String, String>> = coroutineScope {
    val versionLibs = (
        cdnLibs +
            lookupConfig.profiles.values.flatMap { it.deps } +
            lookupConfig.modules.values.flatMap { module -> module.query.deps + module.query.depsAdd }
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
    val module = lookupConfig.modules[lib] ?: return ""
    val inheritedProfile = module.inherits?.let(lookupConfig.profiles::get)
    val profile = resolveModuleQuery(module.query, inheritedProfile)
    val params = mutableListOf<String>()
    if (profile.deps.isNotEmpty()) {
        val deps = profile.deps
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
    val modules: Map<String, CdnLookupModule> = emptyMap(),
)

@Serializable
data class CdnLookupProfile(
    val deps: List<String> = emptyList(),
    val external: List<String> = emptyList(),
)

@Serializable
data class CdnLookupModule(
    val global: String? = null,
    val inherits: String? = null,
    val query: CdnLookupModuleQuery = CdnLookupModuleQuery(),
)

@Serializable
data class CdnLookupModuleQuery(
    val deps: List<String> = emptyList(),
    val external: List<String> = emptyList(),
    val depsAdd: List<String> = emptyList(),
    val externalAdd: List<String> = emptyList(),
)

private fun resolveModuleQuery(moduleQuery: CdnLookupModuleQuery, inheritedProfile: CdnLookupProfile?): CdnLookupProfile {
    val deps = if (moduleQuery.deps.isNotEmpty()) {
        moduleQuery.deps
    } else {
        ((inheritedProfile?.deps ?: emptyList()) + moduleQuery.depsAdd).distinct()
    }
    val external = if (moduleQuery.external.isNotEmpty()) {
        moduleQuery.external
    } else {
        ((inheritedProfile?.external ?: emptyList()) + moduleQuery.externalAdd).distinct()
    }
    return CdnLookupProfile(deps = deps, external = external)
}

suspend fun getVersionForLibrary(lib: String): String {
    val libPackage = resolvePkg(lib, json("cwd" to contextPath))
    val pkg = readPkgUp(json("cwd" to libPackage)).await()
    return pkg["pkg"].unsafeCast<Json>()["version"].unsafeCast<String>()
}
