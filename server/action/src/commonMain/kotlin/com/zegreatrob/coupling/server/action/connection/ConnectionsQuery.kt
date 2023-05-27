package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class ConnectionsQuery(val connectionId: String) :
    SimpleSuspendAction<ConnectionsQuery.Dispatcher, Pair<List<CouplingConnection>, CouplingSocketMessage>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : CouplingConnectionGetSyntax {
        suspend fun perform(command: ConnectionsQuery) = with(command) {
            liveInfoRepository.get(connectionId)
                ?.loadConnections()
        }

        private suspend fun CouplingConnection.loadConnections() = partyId.loadConnections()
            .let { it to couplingSocketMessage(it, null) }
    }
}
