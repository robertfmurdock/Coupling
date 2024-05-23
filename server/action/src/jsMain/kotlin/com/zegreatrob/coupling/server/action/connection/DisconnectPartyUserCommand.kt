package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class DisconnectPartyUserCommand(val connectionId: String) {
    interface Dispatcher : CouplingConnectionGetSyntax, CouplingConnectionDeleteSyntax {
        suspend fun perform(command: DisconnectPartyUserCommand) = with(command) {
            liveInfoRepository.get(connectionId)
                ?.deleteAndLoadRemainingConnections()
        }

        private suspend fun CouplingConnection.deleteAndLoadRemainingConnections() = deleteIt()
            .let { partyId.loadConnections() }
            .let { it to couplingSocketMessage(it, null) }

        private suspend fun CouplingConnection.deleteIt() = deleteConnection(partyId, connectionId)
    }
}
