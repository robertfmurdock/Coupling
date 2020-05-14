package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.TribeIdPlayerRecordsListSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax

object PlayersQuery : SimpleSuspendAction<PlayersQueryDispatcher, List<Record<TribeIdPlayer>>> {
    override val performFunc = link(PlayersQueryDispatcher::perform)
}

interface PlayersQueryDispatcher : CurrentTribeIdSyntax, TribeIdPlayerRecordsListSyntax, FindCallSignActionDispatcher {
    suspend fun perform(query: PlayersQuery) = doWork().successResult()

    private suspend fun doWork(): List<Record<TribeIdPlayer>> {
        val playerRecords = currentTribeId.getPlayerRecords()

        var updatedPlayers = emptyList<Record<TribeIdPlayer>>()
        playerRecords
            .forEachIndexed { _, record ->
                updatedPlayers = updatedPlayers + record.copy(
                    data = record.data.copy(element = record.data.player)
                )

            }
        return updatedPlayers
    }

    private fun findCallSign(updatedPlayers: List<Player>, players: List<Player>, index: Int, player: Player) =
        perform(
            FindCallSignAction(
                players = playersWithNamesSoFar(updatedPlayers, players, index),
                email = player.email
            )
        )

    private fun playersWithNamesSoFar(updatedPlayers: List<Player>, players: List<Player>, index: Int) =
        updatedPlayers + players.subList(index, players.lastIndex)
}
