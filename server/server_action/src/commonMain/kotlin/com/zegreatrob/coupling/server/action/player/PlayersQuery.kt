package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.TribeIdPlayerRecordsListSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult

object PlayersQuery :
    SuspendAction<PlayersQueryDispatcher, List<Record<TribeIdPlayer>>> {
    override suspend fun execute(dispatcher: PlayersQueryDispatcher) = with(dispatcher) { perform() }
}

interface PlayersQueryDispatcher : CurrentTribeIdSyntax, TribeIdPlayerRecordsListSyntax, FindCallSignActionDispatcher {
    suspend fun PlayersQuery.perform(): SuccessfulResult<List<Record<TribeIdPlayer>>> {
        val playerRecords = currentTribeId.getPlayerRecords()

        var updatedPlayers = emptyList<Record<TribeIdPlayer>>()
        playerRecords
            .forEachIndexed { index, record ->
                updatedPlayers = updatedPlayers + record.copy(
                    data = record.data.copy(element = record.data.player)
                )

            }
        return updatedPlayers.successResult()
    }

    private fun findCallSign(updatedPlayers: List<Player>, players: List<Player>, index: Int, player: Player) =
        FindCallSignAction(
            players = playersWithNamesSoFar(updatedPlayers, players, index),
            email = player.email
        )
            .perform()

    private fun playersWithNamesSoFar(updatedPlayers: List<Player>, players: List<Player>, index: Int) =
        updatedPlayers + players.subList(index, players.lastIndex)
}
