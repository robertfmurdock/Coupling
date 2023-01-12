package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class ReportDocCommand(val connectionId: String, val doc: PairAssignmentDocument?) :
    SimpleSuspendAction<ReportDocCommandDispatcher, Pair<List<CouplingConnection>, CouplingSocketMessage>?> {
    override val performFunc = link(ReportDocCommandDispatcher::perform)
}

interface ReportDocCommandDispatcher : CouplingConnectionGetSyntax {
    suspend fun perform(command: ReportDocCommand) = with(command) {
        val connection = liveInfoRepository.get(connectionId)
        if (connection == null) {
            null
        } else {
            loadConnectionsAndGenerateMessage(connection, doc)
        }
    }

    private suspend fun loadConnectionsAndGenerateMessage(
        connection: CouplingConnection,
        document: PairAssignmentDocument?
    ): Pair<List<CouplingConnection>, CouplingSocketMessage> {
        val connections = connection.partyId.loadConnections()
        return connections to couplingSocketMessage(connections, document)
    }
}
