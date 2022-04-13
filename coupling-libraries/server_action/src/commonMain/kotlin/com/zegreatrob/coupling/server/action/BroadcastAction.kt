package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.server.action.connection.DisconnectPartyUserCommand
import com.zegreatrob.coupling.server.action.connection.DisconnectPartyUserCommandDispatcher
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

data class BroadcastAction(val connections: List<CouplingConnection>, val message: Message) :
    SimpleSuspendAction<BroadcastActionDispatcher, Unit> {
    override val performFunc = link(BroadcastActionDispatcher::perform)
}

interface BroadcastActionDispatcher : SocketCommunicator, SuspendActionExecuteSyntax,
    DisconnectPartyUserCommandDispatcher {
    suspend fun perform(action: BroadcastAction) = with(action) {
        println("Broadcasting to ${connections.size} connections")
        connections.mapNotNull { connection ->
            sendMessageAndReturnIdWhenFail(connection.connectionId, message)
        }.forEach {
            execute(DisconnectPartyUserCommand(it))
        }
    }
}
