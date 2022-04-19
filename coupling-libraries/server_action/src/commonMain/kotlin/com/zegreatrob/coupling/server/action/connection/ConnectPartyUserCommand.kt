package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataActionDispatcher
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

data class ConnectPartyUserCommand(val partyId: PartyId, val connectionId: String) :
    SimpleSuspendAction<ConnectPartyUserCommandDispatcher, Pair<List<CouplingConnection>, CouplingSocketMessage>?> {
    override val performFunc = link(ConnectPartyUserCommandDispatcher::perform)
}

interface ConnectPartyUserCommandDispatcher :
    UserIsAuthorizedWithDataActionDispatcher,
    SuspendActionExecuteSyntax,
    CouplingConnectionSaveSyntax,
    CouplingConnectionGetSyntax,
    AuthenticatedUserSyntax {

    suspend fun perform(command: ConnectPartyUserCommand) = with(command) {
        partyId.getAuthorizationData()?.let { (_, players) ->
            CouplingConnection(connectionId, partyId, userPlayer(players, user.email))
                .also { it.save() }
                .let { partyId.loadConnections() }
                .let { it to couplingSocketMessage(it, null) }
        }
    }

    private suspend fun PartyId.getAuthorizationData() = execute(UserIsAuthorizedWithDataAction(this)).valueOrNull()

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
