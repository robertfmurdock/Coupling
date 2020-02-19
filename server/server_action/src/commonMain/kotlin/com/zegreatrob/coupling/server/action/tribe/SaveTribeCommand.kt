package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax
import com.zegreatrob.coupling.server.action.user.UserSaveSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class SaveTribeCommand(val tribe: Tribe) : Action

interface SaveTribeCommandDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax,
    TribeIdGetSyntax,
    TribeSaveSyntax, UserPlayersSyntax, UserSaveSyntax, AuthenticatedUserSyntax {

    override val tribeRepository: TribeRepository

    suspend fun SaveTribeCommand.perform() = logAsync {
        isAuthorizedToSave()
            .whenTrue {
                tribe.save()

                user.copy(authorizedTribeIds = user.authorizedTribeIds + tribe.id)
                    .saveIfUserChanged()
            }
    }

    private suspend fun User.saveIfUserChanged() = if (this != user) save() else Unit

    private suspend fun SaveTribeCommand.isAuthorizedToSave() = getTribeAndPlayers()
        .let { (loadedTribe, players) -> shouldSave(tribe.id, loadedTribe, players) }

    private suspend fun SaveTribeCommand.getTribeAndPlayers() = coroutineScope {
        await(
            async { tribe.id.get() },
            async { getUserPlayers() })
    }

    private fun shouldSave(tribeId: TribeId, loadedTribe: Tribe?, playerList: List<TribeIdPlayer>) =
        tribeIsNew(loadedTribe)
                || playerList.authenticatedTribeIds().contains(tribeId)

    private fun tribeIsNew(existingTribe: Tribe?) = existingTribe == null

    private suspend fun Boolean.whenTrue(block: suspend () -> Unit) = also {
        if (it) {
            block()
        }
    }
}
