package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.UserContextSyntax
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.entity.player.PlayerRepository

object TribeQuery

interface TribeQueryDispatcher : UserContextSyntax {

    val tribeRepository: TribeRepository
    val playerRepository: PlayerRepository

    suspend fun TribeQuery.perform(): List<KtTribe> {
        val (tD, pD) = tribeRepository.getTribesAsync() to playerRepository.getPlayersByEmailAsync(userEmail())
        val tribes = tD.await()
        val players = pD.await()
        val authenticatedTribeIds = players.map { it.tribeId } + userContext.tribeIds.map(::TribeId)
        return tribes.filter { authenticatedTribeIds.contains(it.id) }
    }

}