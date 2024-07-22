package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.repository.cache.CachedContributionRepository
import com.zegreatrob.coupling.server.repository.cache.CachedPairAssignmentDocumentRepository
import com.zegreatrob.coupling.server.repository.cache.CachedPlayerRepository

class CachedRepositoryCatalog(private val catalog: RepositoryCatalog) : RepositoryCatalog by catalog {
    override val contributionRepository by lazy {
        CachedContributionRepository(catalog.contributionRepository)
    }
    override val playerRepository by lazy { CachedPlayerRepository(catalog.playerRepository) }
    override val pairAssignmentDocumentRepository by lazy {
        CachedPairAssignmentDocumentRepository(catalog.pairAssignmentDocumentRepository)
    }
}
