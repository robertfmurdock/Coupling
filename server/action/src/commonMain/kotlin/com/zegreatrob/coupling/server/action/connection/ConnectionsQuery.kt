package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class ConnectionsQuery(val connectionId: String) {

    interface Dispatcher : CouplingConnectionGetSyntax {
        suspend fun perform(command: ConnectionsQuery) = with(command) {
            liveInfoRepository.get(connectionId)
                ?.loadConnections()
        }

        private suspend fun CouplingConnection.loadConnections() = partyId.loadConnections()
            .let { it to couplingSocketMessage(it, null) }
    }
}
