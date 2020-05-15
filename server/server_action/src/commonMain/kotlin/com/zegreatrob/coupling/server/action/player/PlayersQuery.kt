package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.TribeIdPlayerRecordsListSyntax
import com.zegreatrob.coupling.server.action.CurrentTribeIdSyntax

object PlayersQuery : SimpleSuspendAction<PlayersQueryDispatcher, List<Record<TribeIdPlayer>>> {
    override val performFunc = link(PlayersQueryDispatcher::perform)
}

interface PlayersQueryDispatcher : CurrentTribeIdSyntax, TribeIdPlayerRecordsListSyntax, FindCallSignActionDispatcher {
    suspend fun perform(query: PlayersQuery) = doWork().successResult()

    private suspend fun doWork() = currentTribeId.getPlayerRecords().populateMissingCallSigns()

    private fun List<TribeRecord<Player>>.populateMissingCallSigns() = foldIndexed(
        emptyList<Record<TribeIdPlayer>>()
    ) { index, acc, record ->
        val callSign = findCallSign(index, acc, record)

        acc + record.copy(
            data = record.data.copy(element = record.data.player.withPopulatedCallSign(callSign))
        )
    }

    private fun List<TribeRecord<Player>>.findCallSign(
        index: Int,
        acc: List<Record<TribeIdPlayer>>,
        record: TribeRecord<Player>
    ) = record.data.player.email.findCallSign(
        mapPlayers(),
        index,
        acc.mapPlayers()
    )

    private fun List<Record<TribeIdPlayer>>.mapPlayers() = map { it.data.player }

    private fun Player.withPopulatedCallSign(callSign: CallSign) = copy(
        callSignAdjective = callSignAdjective.ifEmpty { callSign.adjective },
        callSignNoun = callSignNoun.ifEmpty { callSign.noun }
    )

    private fun String.findCallSign(
        players: List<Player>,
        index: Int,
        updatedPlayers: List<Player>
    ) = perform(
        FindCallSignAction(
            players = playersWithNamesSoFar(updatedPlayers, players, index),
            email = this
        )
    )

    private fun playersWithNamesSoFar(updatedPlayers: List<Player>, players: List<Player>, index: Int) =
        updatedPlayers + players.subList(index, players.lastIndex)
}
