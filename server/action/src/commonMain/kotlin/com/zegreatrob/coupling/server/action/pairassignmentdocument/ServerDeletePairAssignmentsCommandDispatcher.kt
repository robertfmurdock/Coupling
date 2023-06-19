package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentRecordsSyntax
import com.zegreatrob.coupling.repository.party.PartyIdLoadSyntax
import com.zegreatrob.coupling.repository.slack.SlackAccessGet
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.coupling.server.action.slack.SlackDeleteSpin

interface ServerDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    PairAssignmentDocumentIdDeleteSyntax,
    PartyIdPairAssignmentRecordsSyntax,
    PartyIdLoadSyntax,
    CurrentPartyIdSyntax {

    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

    val slackAccessRepository: SlackAccessGet
    val slackRepository: SlackDeleteSpin

    override suspend fun perform(command: DeletePairAssignmentsCommand): VoidResult {
        val pairAssignments = command.partyId.loadPairAssignmentRecords()
            .elements
            .find { it.id == command.pairAssignmentDocumentId }

        return command.partyIdPairAssignmentId()
            .deleteIt()
            .voidResult()
            .also { pairAssignments?.let { command.partyId.load()?.data?.deleteMessage(it) } }
    }

    private suspend fun Party.deleteMessage(pairAssignments: PairAssignmentDocument) {
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
