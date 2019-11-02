package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.user.UserSaveSyntax

data class SaveTribeCommand(val tribe: KtTribe) : Action

interface SaveTribeCommandDispatcher : ActionLoggingSyntax, UserAuthenticatedTribeIdSyntax, TribeIdGetSyntax, TribeSaveSyntax,
        UserPlayersSyntax, UserSaveSyntax, AuthenticatedUserSyntax {

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

    private suspend fun SaveTribeCommand.getTribeAndPlayers() = getTribeAndPlayersDeferred()
            .let { (tribeDeferred, playerDeferred) ->
                Pair(tribeDeferred.await(), playerDeferred.await())
            }

    private fun SaveTribeCommand.getTribeAndPlayersDeferred() =
            tribe.id.loadAsync() to getUserPlayersAsync()

    private fun shouldSave(tribeId: TribeId, loadedTribe: KtTribe?, playerList: List<TribeIdPlayer>) =
            tribeIsNew(loadedTribe)
                    || playerList.authenticatedTribeIds().contains(tribeId)

    private fun tribeIsNew(existingTribe: KtTribe?) = existingTribe == null

    private suspend fun Boolean.whenTrue(block: suspend () -> Unit) = also {
        if (it) {
            block()
        }
    }
}
