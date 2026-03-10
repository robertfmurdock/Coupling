package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.party.PartySaveSyntax
import com.zegreatrob.coupling.repository.pin.PinSave
import com.zegreatrob.coupling.repository.player.PlayerSave
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
    val playerSaveRepository: PlayerSave
    val pinSaveRepository: PinSave

    override suspend fun perform(command: SavePartyCommand) = if (command.isAuthorizedToSave()) {
        command.savePartyAndUser()
        VoidResult.Accepted
    } else {
        CommandResult.Unauthorized
    }

    private suspend fun SavePartyCommand.savePartyAndUser() = withContext(currentCoroutineContext()) {
        party?.let { partyDetails ->
            launch { partyDetails.save() }
            launch {
                currentUser.copy(authorizedPartyIds = currentUser.authorizedPartyIds + partyDetails.id)
                    .saveIfUserChanged()
            }
        }
        launch { players.forEach { playerSaveRepository.save(PartyElement(partyId, it)) } }
        launch { pins.forEach { pinSaveRepository.save(PartyElement(partyId, it)) } }
    }

    private suspend fun UserDetails.saveIfUserChanged() = if (this != currentUser) save() else Unit

    private suspend fun SavePartyCommand.isAuthorizedToSave(): Boolean {
        val (connectedUsers, loadedParty, players) = coroutineScope {
            await(
                async { loadCurrentConnectedUsers() },
                async { partyId.get() },
                async { currentUser.loadPlayers() },
            )
        }
        val isNewParty = loadedParty.partyIsNew()
        if (isNewParty && party == null) {
            return false
        }
        return isNewParty || authorizedPartyIds(connectedUsers, players).contains(partyId)
    }

    private fun authorizedPartyIds(users: List<UserDetails>, players: List<PartyRecord<Player>>) = players.map { it.data.partyId } + users.flatMap { it.authorizedPartyIds }

    private fun PartyDetails?.partyIsNew() = this == null
}
