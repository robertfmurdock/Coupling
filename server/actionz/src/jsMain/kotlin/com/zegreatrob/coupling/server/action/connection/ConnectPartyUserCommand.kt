package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.action.CannonProvider
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

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

        @OptIn(ExperimentalKotoolsTypesApi::class)
        private fun userPlayer(players: List<Player>, email: NotBlankString): Player {
            val existingPlayer = players.find { it.matches(email.toString()) }

            return if (existingPlayer != null) {
                existingPlayer
            } else {
                val atIndex = email.toString().indexOf("@")
                defaultPlayer.copy(
                    PlayerId("-1".toNotBlankString().getOrThrow()),
                    name = email.toString().substring(0, atIndex),
                    email = email.toString(),
                )
            }
        }
    }
}
