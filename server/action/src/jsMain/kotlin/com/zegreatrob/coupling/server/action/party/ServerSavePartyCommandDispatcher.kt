package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.party.PartySaveSyntax
import com.zegreatrob.coupling.server.action.user.UserSaveSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

interface ServerSavePartyCommandDispatcher :
    SavePartyCommand.Dispatcher,
    UserAuthenticatedPartyIdSyntax,
    PartyIdGetSyntax,
    PartySaveSyntax,
    UserPlayerIdsSyntax,
    UserSaveSyntax,
    CurrentUserProvider {

    override val partyRepository: PartyRepository

    override suspend fun perform(command: SavePartyCommand) = command.isAuthorizedToSave()
        .whenAuthorized { command.savePartyAndUser() }

    private suspend fun SavePartyCommand.savePartyAndUser() = withContext(coroutineContext) {
        launch { party.save() }
        launch {
            currentUser.copy(authorizedPartyIds = currentUser.authorizedPartyIds + party.id)
                .saveIfUserChanged()
        }
    }

    private suspend fun UserDetails.saveIfUserChanged() = if (this != currentUser) save() else Unit

    private suspend fun SavePartyCommand.isAuthorizedToSave() = getPartyAndUserPlayerIds()
        .let { (loadedParty, players) -> shouldSave(party.id, loadedParty, players) }

    private suspend fun SavePartyCommand.getPartyAndUserPlayerIds() = coroutineScope {
        await(
            async { party.id.get() },
            async { getUserPlayerIds() },
        )
    }

    private fun shouldSave(partyId: PartyId, loadedParty: PartyDetails?, playerList: List<PartyElement<String>>) =
        loadedParty.partyIsNew() ||
            playerList.authenticatedPartyIds().contains(partyId)

    private fun PartyDetails?.partyIsNew() = this == null

    private suspend fun Boolean.whenAuthorized(block: suspend () -> Unit) = let {
        if (it) {
            block()
            VoidResult.Accepted
        } else {
            CommandResult.Unauthorized
        }
    }
}
