package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class ReportDocCommand(val connectionId: String, val doc: PairingSet?) {

    interface Dispatcher : CouplingConnectionGetSyntax {
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
            document: PairingSet?,
        ): Pair<List<CouplingConnection>, CouplingSocketMessage> {
            val connections = connection.partyId.loadConnections()
            return connections to couplingSocketMessage(connections, document)
        }
    }
}
