package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class DisconnectTribeUserCommand(val connectionId: String) :
    SimpleSuspendAction<DisconnectTribeUserCommandDispatcher, Pair<List<CouplingConnection>, CouplingSocketMessage>?> {
    override val performFunc = link(DisconnectTribeUserCommandDispatcher::perform)
}

interface DisconnectTribeUserCommandDispatcher : CouplingConnectionGetSyntax, CouplingConnectionDeleteSyntax {
    suspend fun perform(command: DisconnectTribeUserCommand) = with(command) {
        liveInfoRepository.get(connectionId)
            ?.deleteAndLoadRemainingConnections()
    }

    private suspend fun CouplingConnection.deleteAndLoadRemainingConnections() = delete()
        .let { partyId.loadConnections() }
        .let { it to couplingSocketMessage(it, null) }

    private suspend fun CouplingConnection.delete() = deleteConnection(partyId, connectionId)
}

