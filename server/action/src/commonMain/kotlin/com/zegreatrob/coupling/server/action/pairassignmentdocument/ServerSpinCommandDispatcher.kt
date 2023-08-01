package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.discord.DiscordAccessGet
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPinRecordsSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadIntegrationSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadSyntax
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersSyntax
import com.zegreatrob.coupling.repository.slack.SlackAccessGet
import com.zegreatrob.coupling.server.action.CannonProvider
import com.zegreatrob.coupling.server.action.discord.DiscordSendSpin
import com.zegreatrob.coupling.server.action.slack.SlackSendSpin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotools.types.collection.NotEmptyList
import kotools.types.collection.toNotEmptyList

interface ServerSpinCommandDispatcher<out D> :
    SpinCommand.Dispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax,
    PartyIdLoadSyntax,
    PartyIdLoadIntegrationSyntax,
    PartyIdLoadPlayersSyntax,
    PartyIdHistorySyntax,
    PartyIdPinRecordsSyntax,
    CannonProvider<D> where
      D : CreatePairCandidateReportListAction.Dispatcher<D>,
      D : NextPlayerAction.Dispatcher<D>,
      D : FindNewPairsAction.Dispatcher<D>,
      D : ShufflePairsAction.Dispatcher<D>,
      D : AssignPinsAction.Dispatcher,
      D : CreatePairCandidateReportAction.Dispatcher {

    val slackRepository: SlackSendSpin
    val slackAccessRepository: SlackAccessGet
    val discordRepository: DiscordSendSpin
    val discordAccessRepository: DiscordAccessGet
    override val partyRepository: PartyRepository
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

    override suspend fun perform(command: SpinCommand): SpinCommand.Result = coroutineScope {
        with(command) {
            val partyDeferred = async { partyId.load()?.data }
            val partyIntegrationDeferred = async { partyId.loadIntegration() }
            val playersDeferred = async { partyId.loadPlayers().elements }
            val pinsDeferred = async { partyId.loadPins().elements }
            val historyDeferred = async { partyId.loadHistory() }
            return@coroutineScope performSpin(
                partyDeferred.await(),
                partyIntegrationDeferred.await(),
                playersDeferred.await(),
                pinsDeferred.await(),
                historyDeferred.await(),
            )
        }
    }

    private suspend fun SpinCommand.performSpin(
        partyDetails: PartyDetails?,
        partyIntegration: PartyIntegration?,
        allPlayers: List<Player>,
        pins: List<Pin>,
        history: List<PairAssignmentDocument>,
    ): SpinCommand.Result {
        partyDetails ?: return SpinCommand.Result.PartyDoesNotExist(partyId)

        val playersMap = selectedPlayersMap(allPlayers, playerIds)
        if (playersMap.values.any { it == null }) {
            return SpinCommand.Result.CouldNotFindPlayers(
                playersMap.filter { it.value == null }.keys.toNotEmptyList().getOrThrow(),
            )
        }
        val selectedPlayers = playersMap.values.filterNotNull().toNotEmptyList().getOrThrow()
        val action = ShufflePairsAction(
            party = partyDetails,
            players = selectedPlayers,
            pins = filterSelectedPins(pins, pinIds),
            history = history,
        )

        val newPairs = cannon.fire(action)

        partyId.with(newPairs)
            .save()
        coroutineScope {
            launch { partyIntegration?.sendMessage(newPairs) }
            launch {
                val discordAccess = discordAccessRepository.get(partyId)?.data?.element
                discordAccess?.webhook
                    ?.let { discordRepository.sendSpinMessage(it, newPairs) }
                    ?.let { newPairs.copy(discordMessageId = it) }
                    ?.let { partyId.with(it) }
                    ?.save()
            }
        }
        return SpinCommand.Result.Success
    }

    private fun selectedPlayersMap(players: List<Player>, playerIds: NotEmptyList<String>) = playerIds.toList()
        .associateWith { id -> players.find { player -> player.id == id } }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it.id) }

    private suspend fun PartyIntegration.sendMessage(pairs: PairAssignmentDocument) {
        val team = slackTeam ?: return
        val channel = slackChannel ?: return
        val accessRecord = slackAccessRepository.get(team) ?: return
        val token = accessRecord.data.accessToken
        runCatching { slackRepository.sendSpinMessage(channel, token, pairs) }
            .onFailure { it.printStackTrace() }
    }
}
