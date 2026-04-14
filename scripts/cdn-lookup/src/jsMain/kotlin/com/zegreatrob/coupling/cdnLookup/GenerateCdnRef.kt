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
    val versionLibs = (cdnLibs + lookupConfig.query.values.flatMap { it.deps }).distinct()
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
    val profile = lookupConfig.query[lib] ?: return ""
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
    val query: Map<String, CdnLookupProfile> = emptyMap(),
)

@Serializable
data class CdnLookupProfile(
    val deps: List<String> = emptyList(),
    val external: List<String> = emptyList(),
)

suspend fun getVersionForLibrary(lib: String): String {
    val libPackage = resolvePkg(lib, json("cwd" to contextPath))
    val pkg = readPkgUp(json("cwd" to libPackage)).await()
    return pkg["pkg"].unsafeCast<Json>()["version"].unsafeCast<String>()
}
