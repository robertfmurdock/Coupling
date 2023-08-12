package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.repository.player.PlayerEmailRepository

class CachedRepositoryCatalog(private val catalog: RepositoryCatalog) : RepositoryCatalog by catalog {
    override val playerRepository: PlayerEmailRepository by lazy { CachedPlayerRepository(catalog.playerRepository) }
}
