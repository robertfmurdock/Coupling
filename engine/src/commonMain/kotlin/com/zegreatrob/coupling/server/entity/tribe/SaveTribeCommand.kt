package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.AuthenticatedUserSyntax
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.entity.user.UserSaveSyntax

data class SaveTribeCommand(val tribe: KtTribe)

interface SaveTribeCommandDispatcher : UserAuthenticatedTribeIdSyntax, TribeIdGetSyntax, TribeSaveSyntax,
        UserPlayersSyntax, UserSaveSyntax, AuthenticatedUserSyntax {

    suspend fun SaveTribeCommand.perform() = isAuthorizedToSave()
            .whenTrue {
                tribe.save()

                if (!user.authorizedTribeIds.contains(tribe.id)) {
                    user.copy(authorizedTribeIds = user.authorizedTribeIds + tribe.id)
                            .save()
                }
            }

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
