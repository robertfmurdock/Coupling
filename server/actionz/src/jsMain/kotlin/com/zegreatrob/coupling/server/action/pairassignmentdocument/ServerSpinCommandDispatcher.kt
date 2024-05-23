package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.discord.DiscordAccessGet
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistoryTrait
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPinRecordsSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadIntegrationSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadSyntax
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.coupling.repository.slack.SlackAccessGet
import com.zegreatrob.coupling.server.action.CannonProvider
import com.zegreatrob.coupling.server.action.discord.DiscordSendSpin
import com.zegreatrob.coupling.server.action.slack.SlackSendSpin
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotools.types.collection.NotEmptyList
import kotools.types.collection.toNotEmptyList

interface ServerSpinCommandDispatcher<out D> :
    SpinCommand.Dispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax,
    PartyIdLoadSyntax,
    PartyIdLoadIntegrationSyntax,
    PartyIdLoadPlayersTrait,
    PartyIdHistoryTrait,
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
        partyId.with(newPairs.copyWithIntegrationMessageIds(partyId, partyIntegration))
            .save()
        return SpinCommand.Result.Success
    }

    suspend fun PairAssignmentDocument.copyWithIntegrationMessageIds(
        partyId: PartyId,
        partyIntegration: PartyIntegration?,
    ): PairAssignmentDocument {
        val (slackMessageId, discordMessageId) = coroutineScope {
            listOf(
                async { partyIntegration?.sendMessage(this@copyWithIntegrationMessageIds, partyId) },
                async { sendDiscordMessage(partyId, this@copyWithIntegrationMessageIds) },
            ).awaitAll()
        }

        return copy(slackMessageId = slackMessageId, discordMessageId = discordMessageId)
    }

    suspend fun sendDiscordMessage(partyId: PartyId, newPairs: PairAssignmentDocument): String? {
        val discordAccess = discordAccessRepository.get(partyId)?.data?.element
        return discordAccess?.webhook
            ?.let { discordRepository.sendSpinMessage(it, newPairs) }
    }

    private fun selectedPlayersMap(players: List<Player>, playerIds: NotEmptyList<String>) = playerIds.toList()
        .associateWith { id -> players.find { player -> player.id == id } }

    private fun filterSelectedPins(pins: List<Pin>, pinIds: List<String>) = pins.filter { pinIds.contains(it.id) }

    private suspend fun PartyIntegration.sendMessage(pairs: PairAssignmentDocument, partyId: PartyId): String? {
        val team = slackTeam ?: return null
        val channel = slackChannel ?: return null
        val accessRecord = slackAccessRepository.get(team) ?: return null
        val token = accessRecord.data.accessToken
        return slackRepository.sendSpinMessage(channel, token, pairs, partyId)
    }
}
