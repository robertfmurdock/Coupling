package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class DisconnectTribeUserCommand(val tribeId: TribeId, val connectionId: String) :
    SimpleSuspendAction<DisconnectTribeUserCommandDispatcher, CouplingSocketMessage> {
    override val performFunc = link(DisconnectTribeUserCommandDispatcher::perform)
}

interface DisconnectTribeUserCommandDispatcher : CouplingConnectionGetSyntax, CouplingConnectionDeleteSyntax {
    suspend fun perform(command: DisconnectTribeUserCommand) = with(command) {
        tribeId.loadConnections()
            .let { it.filterNot { c -> c.connectionId == connectionId } }
            .also { deleteConnection(tribeId, connectionId) }
            .let { couplingSocketMessage(it, null) }
    }
}