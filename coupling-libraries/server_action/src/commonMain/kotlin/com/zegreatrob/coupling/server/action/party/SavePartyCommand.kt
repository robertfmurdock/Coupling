package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.UnauthorizedResult
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
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

data class SavePartyCommand(val party: Party) :
    SimpleSuspendResultAction<SavePartyCommandDispatcher, Unit> {
    override val performFunc = link(SavePartyCommandDispatcher::perform)
}

interface SavePartyCommandDispatcher :
    UserAuthenticatedPartyIdSyntax,
    PartyIdGetSyntax,
    PartySaveSyntax,
    UserPlayerIdsSyntax,
    UserSaveSyntax,
    AuthenticatedUserSyntax {

    override val partyRepository: PartyRepository

    suspend fun perform(command: SavePartyCommand) = command.isAuthorizedToSave()
        .whenAuthorized { command.savePartyAndUser() }

    private suspend fun SavePartyCommand.savePartyAndUser() = withContext(coroutineContext) {
        launch { party.save() }
        launch {
            user.copy(authorizedPartyIds = user.authorizedPartyIds + party.id)
                .saveIfUserChanged()
        }
    }

    private suspend fun User.saveIfUserChanged() = if (this != user) save() else Unit

    private suspend fun SavePartyCommand.isAuthorizedToSave() = getPartyAndUserPlayerIds()
        .let { (loadedParty, players) -> shouldSave(party.id, loadedParty, players) }

    private suspend fun SavePartyCommand.getPartyAndUserPlayerIds() = coroutineScope {
        await(
            async { party.id.get() },
            async { getUserPlayerIds() }
        )
    }

    private fun shouldSave(partyId: PartyId, loadedParty: Party?, playerList: List<PartyElement<String>>) =
        loadedParty.partyIsNew() ||
            playerList.authenticatedPartyIds().contains(partyId)

    private fun Party?.partyIsNew() = this == null

    private suspend fun Boolean.whenAuthorized(block: suspend () -> Unit): Result<Unit> = let {
        if (it) {
            block().successResult()
        } else {
            UnauthorizedResult()
        }
    }
}
