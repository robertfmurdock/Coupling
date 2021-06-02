package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataActionDispatcher
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

data class ConnectTribeUserCommand(val tribeId: TribeId, val connectionId: String, val user: User) :
    SimpleSuspendAction<ConnectTribeUserCommandDispatcher, CouplingSocketMessage?> {
    override val performFunc = link(ConnectTribeUserCommandDispatcher::perform)
}

interface ConnectTribeUserCommandDispatcher : UserIsAuthorizedWithDataActionDispatcher, SuspendActionExecuteSyntax,
    CouplingConnectionSaveSyntax, CouplingConnectionGetSyntax {

    suspend fun perform(command: ConnectTribeUserCommand) = with(command) {
        tribeId.getAuthorizationData()?.let { (_, players) ->
            CouplingConnection(connectionId, tribeId, userPlayer(players, user.email))
                .also { it.save() }
                .let { tribeId.loadConnections() }
                .let { couplingSocketMessage(it, null) }
        }
    }

    private suspend fun TribeId.getAuthorizationData() = execute(UserIsAuthorizedWithDataAction(this)).valueOrNull()

    private fun userPlayer(players: List<Player>, email: String): Player {
        val existingPlayer = players.find { it.email == email }

        return if (existingPlayer != null) {
            existingPlayer
        } else {
            val atIndex = email.indexOf("@")
            Player("-1", name = email.substring(0, atIndex), email = email)
        }
    }

}