package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.UserContextSyntax
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.entity.player.PlayerRepository

object TribeQuery

interface TribeQueryDispatcher : UserContextSyntax {

    val tribeRepository: TribeRepository
    val playerRepository: PlayerRepository

    suspend fun TribeQuery.perform() = getTribesAndPlayers().onlyAuthenticatedTribes()

    private suspend fun getTribesAndPlayers() = getTribesAndPlayersDeferred()
            .let { (tribeDeferred, playerDeferred) ->
                Pair(tribeDeferred.await(), playerDeferred.await())
            }

    private fun getTribesAndPlayersDeferred() =
            tribeRepository.getTribesAsync() to playerRepository.getPlayersByEmailAsync(userEmail())

    private fun Pair<List<KtTribe>, List<TribeIdPlayer>>.onlyAuthenticatedTribes() = let { (tribes, players) ->
        tribes.filter(players.isAuthenticatedFilter())
    }

    private fun List<TribeIdPlayer>.isAuthenticatedFilter() = authenticatedTribeIds().isAuthenticatedFilter()

    private fun List<TribeIdPlayer>.authenticatedTribeIds() = map { it.tribeId } + userContext.tribeIds.map(::TribeId)

    private fun List<TribeId>.isAuthenticatedFilter(): (KtTribe) -> Boolean = { contains(it.id) }

}