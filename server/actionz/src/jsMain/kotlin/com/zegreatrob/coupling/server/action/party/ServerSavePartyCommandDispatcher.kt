package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.party.PartySaveSyntax
import com.zegreatrob.coupling.server.action.user.UserSaveSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ServerSavePartyCommandDispatcher :
    SavePartyCommand.Dispatcher,
    CurrentConnectedUsersProvider,
    PartyIdGetSyntax,
    PartySaveSyntax,
    UserPlayersSyntax,
    UserSaveSyntax,
    CurrentUserProvider {

    override val partyRepository: PartyRepository

    override suspend fun perform(command: SavePartyCommand) = if (command.isAuthorizedToSave()) {
        command.savePartyAndUser()
        VoidResult.Accepted
    } else {
        CommandResult.Unauthorized
    }

    private suspend fun SavePartyCommand.savePartyAndUser() = withContext(currentCoroutineContext()) {
        launch { party.save() }
        launch {
            currentUser.copy(authorizedPartyIds = currentUser.authorizedPartyIds + party.id)
                .saveIfUserChanged()
        }
    }

    private suspend fun UserDetails.saveIfUserChanged() = if (this != currentUser) save() else Unit

    private suspend fun SavePartyCommand.isAuthorizedToSave(): Boolean {
        val (connectedUsers, loadedParty, players) = coroutineScope {
            await(
                async { loadCurrentConnectedUsers() },
                async { party.id.get() },
                async { currentUser.loadPlayers() },
            )
        }
        return loadedParty.partyIsNew() || authorizedPartyIds(connectedUsers, players).contains(party.id)
    }

    private fun authorizedPartyIds(users: List<UserDetails>, players: List<PartyRecord<Player>>) = players.map { it.data.partyId } + users.flatMap { it.authorizedPartyIds }

    private fun PartyDetails?.partyIsNew() = this == null
}
