package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadIntegrationSyntax
import com.zegreatrob.coupling.repository.slack.SlackAccessGet
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.coupling.server.action.discord.DiscordDeleteSpin
import com.zegreatrob.coupling.server.action.slack.SlackDeleteSpin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

interface ServerDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    PairAssignmentDocumentIdDeleteSyntax,
    PartyIdPairAssignmentRecordsSyntax,
    PartyIdLoadIntegrationSyntax,
    CurrentPartyIdSyntax {

    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

    val slackAccessRepository: SlackAccessGet
    val slackRepository: SlackDeleteSpin
    val discordAccessRepository: DiscordAccessRepository
    val discordRepository: DiscordDeleteSpin

    override suspend fun perform(command: DeletePairAssignmentsCommand): VoidResult {
        val partyId = command.partyId
        val pairAssignments = partyId.loadPairAssignmentRecords()
            .elements
            .find { it.id == command.pairAssignmentDocumentId }

        return command.partyIdPairAssignmentId()
            .deleteIt()
            .voidResult()
            .also { pairAssignments?.let(partyId::with)?.deleteIntegrationMessages() }
    }

    private suspend fun PartyElement<PairAssignmentDocument>.deleteIntegrationMessages() = coroutineScope {
        launch { deleteDiscordMessage() }
        launch { deleteSlackMessage() }
    }

    private suspend fun PartyElement<PairAssignmentDocument>.deleteSlackMessage() {
        partyId.loadIntegration()?.deleteMessage(element)
    }

    private suspend fun PartyElement<PairAssignmentDocument>.deleteDiscordMessage() {
        partyId.getDiscordTeamAccess()
            ?.deleteMessage(element)
    }

    private suspend fun DiscordTeamAccess.deleteMessage(pairAssignmentDocument: PairAssignmentDocument) =
        discordRepository.deleteMessage(webhook, pairAssignmentDocument)

    private suspend fun PartyId.getDiscordTeamAccess() = discordAccessRepository.get(this)?.data?.element

    private suspend fun PartyIntegration.deleteMessage(pairAssignments: PairAssignmentDocument) {
        val team = slackTeam ?: return
        val channel = slackChannel ?: return
        val accessRecord = slackAccessRepository.get(team) ?: return
        val token = accessRecord.data.accessToken
        runCatching { slackRepository.deleteSpinMessage(channel, token, pairAssignments) }
            .onFailure { it.printStackTrace() }
    }

    private fun DeletePairAssignmentsCommand.partyIdPairAssignmentId() = PartyIdPairAssignmentDocumentId(
        currentPartyId,
        pairAssignmentDocumentId,
    )
}
