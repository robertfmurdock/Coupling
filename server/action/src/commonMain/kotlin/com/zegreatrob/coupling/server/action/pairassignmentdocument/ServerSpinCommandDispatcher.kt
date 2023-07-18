package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
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
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    CannonProvider<D> where D : NextPlayerAction.Dispatcher,
      D : FindNewPairsAction.Dispatcher<D>,
      D : AssignPinsAction.Dispatcher,
      D : ShufflePairsAction.Dispatcher<D> {

    val slackRepository: SlackRepository
    val slackAccessRepository: SlackAccessGet
    override val partyRepository: PartyRepository
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

    override suspend fun perform(command: SpinCommand): VoidResult {
        val (shufflePairsAction, integration) = command.shufflePairsAction()
            ?: return VoidResult.Rejected

        val newPairs = cannon.fire(shufflePairsAction)

        command.partyId.with(newPairs)
            .save()

        integration?.sendMessage(newPairs)
        return VoidResult.Accepted
    }

    private suspend fun SpinCommand.shufflePairsAction() = coroutineScope {
        val partyDeferred = async { partyId.load()?.data }
        val partyIntegrationDeferred = async { partyId.loadIntegration() }
        val playersDeferred = async { partyId.loadPlayers().elements }
        val pinsDeferred = async { partyId.loadPins().elements }
        val historyDeferred = async { partyId.loadHistory() }
        val players = filterSelectedPlayers(playersDeferred.await(), playerIds)
            .toNotEmptyList()
            .getOrNull()
            ?: return@coroutineScope null
        ShufflePairsAction(
            party = partyDeferred.await()
                ?: return@coroutineScope null,
            players = players,
            pins = filterSelectedPins(pinsDeferred.await(), pinIds),
            history = historyDeferred.await(),
        ) to partyIntegrationDeferred.await()
    }

    private fun filterSelectedPlayers(players: List<Player>, playerIds: NotEmptyList<String>) =
        playerIds.toList().mapNotNull { id -> players.find { player -> player.id == id } }

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
