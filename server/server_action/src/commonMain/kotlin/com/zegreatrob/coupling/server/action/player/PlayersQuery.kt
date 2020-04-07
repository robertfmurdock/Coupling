package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.TribeIdPlayerRecordsListSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax

object PlayersQuery : Action

interface PlayersQueryDispatcher : ActionLoggingSyntax, CurrentTribeIdSyntax, TribeIdPlayerRecordsListSyntax,
    FindCallSignActionDispatcher {
    suspend fun PlayersQuery.perform() = logAsync {
        val playerRecords = currentTribeId.getPlayerRecords()

        var updatedPlayers = emptyList<Record<TribeIdPlayer>>()
        playerRecords
            .forEachIndexed { index, record ->

                val callSign = findCallSign(
                    updatedPlayers.map { it.data.player },
                    playerRecords.map { it.data.player },
                    index,
                    record.data.player
                )

                updatedPlayers = updatedPlayers + record.copy(
                    data = record.data.copy(element = record.data.player.withCallSign(callSign))
                )

            }
        updatedPlayers
    }

    private fun Player.withCallSign(callSign: CallSign) = copy(
        callSignAdjective = callSignAdjective,
        callSignNoun = callSignNoun
    )

    private fun findCallSign(updatedPlayers: List<Player>, players: List<Player>, index: Int, player: Player) =
        FindCallSignAction(
            players = playersWithNamesSoFar(updatedPlayers, players, index),
            email = player.email
        )
            .perform()

    private fun playersWithNamesSoFar(updatedPlayers: List<Player>, players: List<Player>, index: Int) =
        updatedPlayers + players.subList(index, players.lastIndex)
}
