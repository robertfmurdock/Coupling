package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.server.action.CannonProvider
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class ConnectPartyUserCommand(val partyId: PartyId, val connectionId: String) {

    interface Dispatcher<out D> :
        CannonProvider<D>,
        CouplingConnectionSaveSyntax,
        CouplingConnectionGetSyntax,
        CurrentUserProvider where D : UserIsAuthorizedWithDataAction.Dispatcher {

        suspend fun perform(command: ConnectPartyUserCommand) = with(command) {
            partyId.getAuthorizationData()?.let { (_, players) ->
                CouplingConnection(connectionId, partyId, userPlayer(players, currentUser.email))
                    .also { it.save() }
                    .let { partyId.loadConnections() }
                    .let { it to couplingSocketMessage(it, null) }
            }
        }

        private suspend fun PartyId.getAuthorizationData() = cannon.fire(UserIsAuthorizedWithDataAction(this))
            .valueOrNull()

        private fun userPlayer(players: List<Player>, email: String): Player {
            val existingPlayer = players.find { it.matches(email) }

            return if (existingPlayer != null) {
                existingPlayer
            } else {
                val atIndex = email.indexOf("@")
                com.zegreatrob.coupling.model.player.defaultPlayer.copy(
                    "-1",
                    name = email.substring(0, atIndex),
                    email = email,
                )
            }
        }
    }
}
