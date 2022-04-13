package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.action.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.PartyPlayerRecordsListSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

object PlayersQuery : SimpleSuspendResultAction<PlayersQueryDispatcher, List<Record<PartyElement<Player>>>> {
    override val performFunc = link(PlayersQueryDispatcher::perform)
}

interface PlayersQueryDispatcher : CurrentPartyIdSyntax, PartyPlayerRecordsListSyntax, FindCallSignActionDispatcher {
    suspend fun perform(query: PlayersQuery) = doWork().successResult()

    private suspend fun doWork() = currentPartyId.getPlayerRecords().populateMissingCallSigns()

    private fun List<PartyRecord<Player>>.populateMissingCallSigns() = foldIndexedToList { index, acc, record ->
        val callSign = findCallSign(index, acc, record)

        record.copy(data = record.data.copy(element = record.data.player.withPopulatedCallSign(callSign)))
    }

    private fun <T1> List<T1>.foldIndexedToList(operation: (index: Int, acc: List<T1>, T1) -> T1): List<T1> =
        foldIndexed(emptyList()) { index, acc, record -> acc + operation(index, acc, record) }

    private fun List<PartyRecord<Player>>.findCallSign(
        index: Int,
        acc: List<Record<PartyElement<Player>>>,
        record: PartyRecord<Player>
    ) = record.data.player.email.findCallSign(
        mapPlayers(),
        index,
        acc.mapPlayers()
    )

    private fun List<Record<PartyElement<Player>>>.mapPlayers() = map { it.data.player }

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
