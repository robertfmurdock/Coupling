package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.SaveBoostCommand
import com.zegreatrob.coupling.action.SubscriptionCommandResult
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.server.action.subscription.UserDetailsSubscriptionTrait
import com.zegreatrob.coupling.server.action.subscription.active

interface ServerSaveBoostCommandDispatcher :
    BoostSaveSyntax,
    CurrentUserProvider,
    UserDetailsSubscriptionTrait,
    SaveBoostCommand.Dispatcher {

    override suspend fun perform(command: SaveBoostCommand) = command.save()

    private suspend fun SaveBoostCommand.save(): SaveBoostCommand.Result {
        currentUser.subscriptionDetails()
            .active()
            ?: return SubscriptionCommandResult.SubscriptionNotActive

        Boost(currentUser.id, partyIds)
            .apply { save() }
        return SaveBoostCommand.Result.Success
    }
}
