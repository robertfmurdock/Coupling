package com.zegreatrob.coupling.cdnLookup

import kotlinx.serialization.Serializable

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

internal fun CdnLookupConfig.versionLibraries(cdnLibs: List<String>): List<String> = (
    cdnLibs +
        profiles.values.flatMap { it.dependencies } +
        imports.values.flatMap { it.query.dependencies }
    ).distinct()

internal fun CdnLookupConfig.configuredSingletonDependencies(): Set<String> = (
    profiles.values.flatMap { it.dependencies } +
        imports.values.flatMap { it.query.dependencies }
    ).toSet()

internal fun CdnLookupConfig.configuredDependencyGroups(): List<Set<String>> = buildList {
    addAll(profiles.values.map { it.dependencies.toSet() })
    addAll(imports.values.map { it.query.dependencies.toSet() })
}.filter { it.isNotEmpty() }

internal fun resolveImportQuery(query: CdnLookupProfile, inheritedProfile: CdnLookupProfile?): CdnLookupProfile {
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
