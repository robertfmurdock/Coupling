package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class DisconnectPartyUserCommand(val connectionId: String) :
    SimpleSuspendAction<DisconnectPartyUserCommandDispatcher, Pair<List<CouplingConnection>, CouplingSocketMessage>?> {
    override val performFunc = link(DisconnectPartyUserCommandDispatcher::perform)
}

interface DisconnectPartyUserCommandDispatcher : CouplingConnectionGetSyntax, CouplingConnectionDeleteSyntax {
    suspend fun perform(command: DisconnectPartyUserCommand) = with(command) {
        liveInfoRepository.get(connectionId)
            ?.deleteAndLoadRemainingConnections()
    }

    private suspend fun CouplingConnection.deleteAndLoadRemainingConnections() = delete()
        .let { partyId.loadConnections() }
        .let { it to couplingSocketMessage(it, null) }

    private suspend fun CouplingConnection.delete() = deleteConnection(partyId, connectionId)
}
