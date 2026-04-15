package com.zegreatrob.coupling.cdnLookup

internal fun validateLookupConfig(cdnLibs: List<String>, lookupConfig: CdnLookupConfig) {
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
    val unknownDependencies = lookupConfig
        .configuredSingletonDependencies()
        .filterNot(availableDependencies::contains)
    if (unknownDependencies.isNotEmpty()) {
        error("Unknown dependencies in CDN settings: ${unknownDependencies.joinToString(", ")}")
    }
}
