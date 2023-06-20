package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
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
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface ServerSpinCommandDispatcher :
    SpinCommand.Dispatcher,
    ShufflePairsAction.Dispatcher,
    SuspendActionExecuteSyntax,
    PartyIdPairAssignmentDocumentSaveSyntax,
    PartyIdLoadSyntax,
    PartyIdLoadIntegrationSyntax,
    PartyIdLoadPlayersSyntax,
    PartyIdHistorySyntax,
    PartyIdPinRecordsSyntax {

    val slackRepository: SlackRepository
    val slackAccessRepository: SlackAccessGet
    override val partyRepository: PartyRepository
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

    override suspend fun perform(command: SpinCommand): VoidResult {
        val (shufflePairsAction, integration) = command.shufflePairsAction()
            ?: return VoidResult.Rejected

        val newPairs = execute(shufflePairsAction)

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
        ShufflePairsAction(
            party = partyDeferred.await()
                ?: return@coroutineScope null,
            players = filterSelectedPlayers(playersDeferred.await(), playerIds),
            pins = filterSelectedPins(pinsDeferred.await(), pinIds),
            history = historyDeferred.await(),
        ) to partyIntegrationDeferred.await()
    }

    private fun filterSelectedPlayers(players: List<Player>, playerIds: List<String>) = players.filter {
        playerIds.contains(it.id)
    }

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
