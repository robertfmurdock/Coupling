package com.zegreatrob.coupling.server

class CachedRepositoryCatalog(private val catalog: RepositoryCatalog) : RepositoryCatalog by catalog {
    override val contributionRepository by lazy { CachedContributionRepository(catalog.contributionRepository) }
    override val playerRepository by lazy { CachedPlayerRepository(catalog.playerRepository) }
    override val pairAssignmentDocumentRepository by lazy {
        CachedPairAssignmentDocumentRepository(catalog.pairAssignmentDocumentRepository)
    }
}
