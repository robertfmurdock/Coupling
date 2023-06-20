package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadIntegrationSyntax
import com.zegreatrob.coupling.repository.slack.SlackAccessGet
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.connection.CouplingConnectionGetSyntax
import com.zegreatrob.coupling.server.action.slack.SlackUpdateSpin
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

interface ServerSavePairAssignmentDocumentCommandDispatcher :
    SavePairAssignmentsCommand.Dispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax,
    CouplingConnectionGetSyntax,
    PartyIdLoadIntegrationSyntax,
    SuspendActionExecuteSyntax,
    BroadcastAction.Dispatcher {

    val slackRepository: SlackUpdateSpin
    val slackAccessRepository: SlackAccessGet

    override suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
        partyId.with(pairAssignments)
            .apply { save() }
            .apply { execute(broadcastAction()) }
            .let { VoidResult.Accepted }
            .also { partyId.loadIntegration()?.updateMessage(pairAssignments) }
    }

    private suspend fun PartyIntegration.updateMessage(pairs: PairAssignmentDocument) {
        val team = slackTeam ?: return
        val channel = slackChannel ?: return
        val accessRecord = slackAccessRepository.get(team) ?: return
        val token = accessRecord.data.accessToken
        runCatching { slackRepository.updateSpinMessage(channel, token, pairs) }
            .onFailure { it.printStackTrace() }
    }

    suspend fun SavePairAssignmentsCommand.broadcastAction() = BroadcastAction(
        partyId.loadConnections(),
        PairAssignmentAdjustmentMessage(pairAssignments),
    )
}
