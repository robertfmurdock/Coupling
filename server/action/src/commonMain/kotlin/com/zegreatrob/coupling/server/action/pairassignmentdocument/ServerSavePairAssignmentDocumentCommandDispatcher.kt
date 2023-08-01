package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.discord.DiscordAccessGet
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadIntegrationSyntax
import com.zegreatrob.coupling.repository.slack.SlackAccessGet
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.CannonProvider
import com.zegreatrob.coupling.server.action.connection.CouplingConnectionGetSyntax
import com.zegreatrob.coupling.server.action.connection.DisconnectPartyUserCommand
import com.zegreatrob.coupling.server.action.discord.DiscordSendSpin
import com.zegreatrob.coupling.server.action.fire
import com.zegreatrob.coupling.server.action.slack.SlackSendSpin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

interface ServerSavePairAssignmentDocumentCommandDispatcher<out D> :
    SavePairAssignmentsCommand.Dispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax,
    CouplingConnectionGetSyntax,
    CannonProvider<D>,
    PartyIdLoadIntegrationSyntax
    where D : DisconnectPartyUserCommand.Dispatcher, D : BroadcastAction.Dispatcher<D> {

    val slackRepository: SlackSendSpin
    val slackAccessRepository: SlackAccessGet
    val discordRepository: DiscordSendSpin
    val discordAccessRepository: DiscordAccessGet

    override suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
        partyId.with(pairAssignments)
            .apply { save() }
            .apply { cannon.fire(broadcastAction()) }
            .let { VoidResult.Accepted }
            .also { partyId.loadIntegration()?.updateMessage(pairAssignments, partyId) }
    }

    private suspend fun PartyIntegration.updateMessage(pairs: PairAssignmentDocument, partyId: PartyId) {
        val team = slackTeam ?: return
        val channel = slackChannel ?: return
        val accessRecord = slackAccessRepository.get(team) ?: return
        val token = accessRecord.data.accessToken

        coroutineScope {
            launch {
                if (pairs.slackMessageId != null) {
                    slackRepository.sendSpinMessage(channel, token, pairs)
                }
            }

            launch {
                if (pairs.discordMessageId != null) {
                    discordAccessRepository.get(partyId)?.data?.element?.let { discordTeamAccess ->
                        discordRepository.sendSpinMessage(discordTeamAccess.webhook, pairs)
                    }
                }
            }
        }
    }

    suspend fun SavePairAssignmentsCommand.broadcastAction() = BroadcastAction(
        partyId.loadConnections(),
        PairAssignmentAdjustmentMessage(pairAssignments),
    )
}
