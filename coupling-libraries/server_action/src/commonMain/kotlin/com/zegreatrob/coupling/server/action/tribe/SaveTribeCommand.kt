package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.UnauthorizedResult
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyElement
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax
import com.zegreatrob.coupling.server.action.user.UserSaveSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

data class SaveTribeCommand(val tribe: Party) :
    SimpleSuspendResultAction<SaveTribeCommandDispatcher, Unit> {
    override val performFunc = link(SaveTribeCommandDispatcher::perform)
}

interface SaveTribeCommandDispatcher : UserAuthenticatedTribeIdSyntax, TribeIdGetSyntax, TribeSaveSyntax,
    UserPlayerIdsSyntax, UserSaveSyntax, AuthenticatedUserSyntax {

    override val tribeRepository: TribeRepository

    suspend fun perform(command: SaveTribeCommand) = command.isAuthorizedToSave()
        .whenAuthorized { command.saveTribeAndUser() }

    private suspend fun SaveTribeCommand.saveTribeAndUser() = withContext(coroutineContext) {
        launch { tribe.save() }
        launch {
            user.copy(authorizedPartyIds = user.authorizedPartyIds + tribe.id)
                .saveIfUserChanged()
        }
    }

    private suspend fun User.saveIfUserChanged() = if (this != user) save() else Unit

    private suspend fun SaveTribeCommand.isAuthorizedToSave() = getTribeAndUserPlayerIds()
        .let { (loadedTribe, players) -> shouldSave(tribe.id, loadedTribe, players) }

    private suspend fun SaveTribeCommand.getTribeAndUserPlayerIds() = coroutineScope {
        await(
            async { tribe.id.get() },
            async { getUserPlayerIds() })
    }

    private fun shouldSave(tribeId: PartyId, loadedTribe: Party?, playerList: List<PartyElement<String>>) =
        tribeIsNew(loadedTribe)
                || playerList.authenticatedTribeIds().contains(tribeId)

    private fun tribeIsNew(existingTribe: Party?) = existingTribe == null

    private suspend fun Boolean.whenAuthorized(block: suspend () -> Unit): Result<Unit> = let {
        if (it) {
            block().successResult()
        } else {
            UnauthorizedResult()
        }
    }
}
