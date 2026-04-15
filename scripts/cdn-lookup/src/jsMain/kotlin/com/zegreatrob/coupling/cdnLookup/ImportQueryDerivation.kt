package com.zegreatrob.coupling.cdnLookup

import kotlin.js.Json

internal data class QueryDerivationContext(
    val singletonDependencies: Set<String>,
    val dependencyGroups: List<Set<String>>,
    val availableImports: Set<String>,
)

internal fun CdnLookupConfig.toQueryDerivationContext(): QueryDerivationContext = QueryDerivationContext(
    singletonDependencies = configuredSingletonDependencies(),
    dependencyGroups = configuredDependencyGroups(),
    availableImports = imports.keys,
)

internal suspend fun queryParametersFor(
    lib: String,
    versions: Map<String, String>,
    lookupConfig: CdnLookupConfig,
    derivationContext: QueryDerivationContext,
): String {
    val item = lookupConfig.imports[lib] ?: return ""
    val inheritedProfile = item.profile?.let(lookupConfig.profiles::get)
    val configuredQuery = resolveImportQuery(item.query, inheritedProfile)
    val effectiveProfile = if (configuredQuery.dependencies.isNotEmpty() || configuredQuery.external.isNotEmpty()) {
        configuredQuery
    } else {
        deriveImportQueryFromPackage(lib, derivationContext)
    }

    val params = mutableListOf<String>()
    if (effectiveProfile.dependencies.isNotEmpty()) {
        val deps = effectiveProfile.dependencies
            .joinToString(",") { dependency -> "$dependency@${versions.getValue(dependency)}" }
        params.add("deps=$deps")
    }
    if (effectiveProfile.external.isNotEmpty()) {
        val external = effectiveProfile.external
            .joinToString(",") { dependency -> encodeQueryParamValue(dependency) }
        params.add("external=$external")
    }

    return if (params.isEmpty()) "" else "?${params.joinToString("&")}"
}

private fun encodeQueryParamValue(value: String): String = js("encodeURIComponent")(value).unsafeCast<String>()

private suspend fun deriveImportQueryFromPackage(
    lib: String,
    derivationContext: QueryDerivationContext,
): CdnLookupProfile {
    val packageName = packageNameForImport(lib)
    val derivedDependencies = collectPeerDependenciesFromPackageGraph(packageName, derivationContext.availableImports)
        .filter(derivationContext.singletonDependencies::contains)
        .let {
            expandDependencyGroups(
                derivedDependencies = it.toSet(),
                dependencyGroups = derivationContext.dependencyGroups,
                availableImports = derivationContext.availableImports,
            )
        }

    if (derivedDependencies.isEmpty()) {
        return CdnLookupProfile()
    }

    val derivedExternal = buildList {
        addAll(derivedDependencies)
        derivedDependencies.forEach { dependency ->
            val jsxRuntimeImport = "$dependency/jsx-runtime"
            if (derivationContext.availableImports.contains(jsxRuntimeImport)) {
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
        collectMatchingPeers(pkg, availableImports)?.let { matchingPeers ->
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

        if (minimumPeerDepth != null && depth >= minimumPeerDepth) {
            continue
        }

        packageDependencies(pkg).forEach { dependency ->
            queue.addLast(dependency to depth + 1)
        }
    }

    return peers.toList().distinct()
}

private fun collectMatchingPeers(pkg: Json, availableImports: Set<String>): List<String>? {
    val peerDependencies = pkg["peerDependencies"]?.unsafeCast<Json?>() ?: return null
    val matchingPeers = objectKeys(peerDependencies).filter(availableImports::contains)
    return matchingPeers.takeIf { it.isNotEmpty() }
}

private fun packageDependencies(pkg: Json): List<String> {
    val dependencies = pkg["dependencies"]?.unsafeCast<Json?>() ?: return emptyList()
    return objectKeys(dependencies)
}

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
