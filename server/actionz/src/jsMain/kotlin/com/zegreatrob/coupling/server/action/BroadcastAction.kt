package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.CannonProvider
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.server.action.connection.DisconnectPartyUserCommand
import com.zegreatrob.coupling.server.action.connection.fire
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class BroadcastAction(val connections: List<CouplingConnection>, val message: Message) {
    interface Dispatcher<out D> :
        SocketCommunicator,
        CannonProvider<D> where D : DisconnectPartyUserCommand.Dispatcher {
        suspend fun perform(action: BroadcastAction) = with(action) {
            println("Broadcasting to ${connections.size} connections")
            connections.mapNotNull { connection ->
                sendMessageAndReturnIdWhenFail(connection.connectionId, message)
            }.forEach {
                cannon.fire(DisconnectPartyUserCommand(it))
            }
        }
    }
}
