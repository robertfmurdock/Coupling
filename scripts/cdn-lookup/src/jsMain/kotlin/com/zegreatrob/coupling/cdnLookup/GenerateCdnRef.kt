package com.zegreatrob.coupling.cdnLookup

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun generateCdnRef(
    cdnLibs: List<String>,
    lookupConfig: CdnLookupConfig = CdnLookupConfig(),
): List<Pair<String, String>> = coroutineScope {
    validateLookupConfig(cdnLibs, lookupConfig)
    val versions = resolveVersions(cdnLibs, lookupConfig)
    val derivationContext = lookupConfig.toQueryDerivationContext()

    cdnLibs
        .map { lib -> async { lookupCdnUrl(lib, versions, lookupConfig, derivationContext) } }
        .awaitAll()
}

private suspend fun resolveVersions(
    cdnLibs: List<String>,
    lookupConfig: CdnLookupConfig,
): Map<String, String> = coroutineScope {
    lookupConfig.versionLibraries(cdnLibs)
        .map { lib -> async { lib to getVersionForLibrary(lib) } }
        .awaitAll()
        .toMap()
}

private suspend fun lookupCdnUrl(
    lib: String,
    versions: Map<String, String>,
    lookupConfig: CdnLookupConfig,
    derivationContext: QueryDerivationContext,
): Pair<String, String> {
    val version = versions.getValue(lib)
    val (module, submodule) = lib.moduleAndSubmodule()
    val queryParameters = queryParametersFor(lib, versions, lookupConfig, derivationContext)

    return lib to "https://esm.sh/$module@$version$submodule$queryParameters"
}

private fun String.moduleAndSubmodule(): Pair<String, String> {
    val split = indexOf("/")
    return if (startsWith("@") || split < 0) {
        this to ""
    } else {
        take(split) to substring(split)
    }
}
