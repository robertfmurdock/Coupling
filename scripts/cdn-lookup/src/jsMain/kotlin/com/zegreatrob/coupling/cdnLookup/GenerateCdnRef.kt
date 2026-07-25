package com.zegreatrob.coupling.cdnLookup

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun generateCdnRef(
    cdnLibs: List<String>,
    lookupConfig: CdnLookupConfig = CdnLookupConfig(),
    workingDirectory: String = currentWorkingDirectory(),
): List<Pair<String, String>> = generateCdnLookup(cdnLibs, lookupConfig, workingDirectory).urls.toList()

suspend fun generateCdnLookup(
    cdnLibs: List<String>,
    lookupConfig: CdnLookupConfig = CdnLookupConfig(),
    workingDirectory: String = currentWorkingDirectory(),
): CdnLookupResult = generateCdnLookup(cdnLibs, lookupConfig, workingDirectory, selectedCdnProvider())

internal suspend fun generateCdnLookup(
    cdnLibs: List<String>,
    lookupConfig: CdnLookupConfig = CdnLookupConfig(),
    workingDirectory: String = currentWorkingDirectory(),
    cdnProvider: CdnProvider,
): CdnLookupResult = coroutineScope {
    validateLookupConfig(cdnLibs, lookupConfig)
    val packageMetadata = PackageMetadata(workingDirectory)
    val versions = resolveVersions(cdnLibs, lookupConfig, packageMetadata)
    val derivationContext = lookupConfig.toQueryDerivationContext()

    val urls = cdnLibs
        .map { lib -> async { lookupCdnUrl(lib, versions, lookupConfig, derivationContext, packageMetadata, cdnProvider) } }
        .awaitAll()
        .toMap()
    CdnLookupResult(urls)
}

private suspend fun resolveVersions(
    cdnLibs: List<String>,
    lookupConfig: CdnLookupConfig,
    packageMetadata: PackageMetadata,
): Map<String, String> = coroutineScope {
    lookupConfig.versionLibraries(cdnLibs)
        .map { lib -> async { lib to packageMetadata.versionForLibrary(lib) } }
        .awaitAll()
        .toMap()
}

private suspend fun lookupCdnUrl(
    lib: String,
    versions: Map<String, String>,
    lookupConfig: CdnLookupConfig,
    derivationContext: QueryDerivationContext,
    packageMetadata: PackageMetadata,
    cdnProvider: CdnProvider,
): Pair<String, String> {
    val version = versions.getValue(lib)
    val profile = queryProfileFor(lib, lookupConfig, derivationContext, packageMetadata)
    val url = cdnProvider.urlFor(
        CdnProviderRequest(
            importName = lib,
            version = version,
            dependencies = profile.dependencies.map { dependency ->
                CdnProviderDependency(dependency, versions.getValue(dependency))
            },
            external = profile.external,
        ),
    )

    return lib to url
}
