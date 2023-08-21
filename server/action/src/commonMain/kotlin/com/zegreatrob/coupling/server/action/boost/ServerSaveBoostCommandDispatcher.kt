package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.action.SubscriptionCommandResult
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.CurrentUserProvider
import com.zegreatrob.coupling.server.action.subscription.UserDetailsSubscriptionTrait
import com.zegreatrob.coupling.server.action.subscription.activeExpiration

interface ServerSaveBoostCommandDispatcher :
    BoostSaveSyntax,
    CurrentUserProvider,
    UserDetailsSubscriptionTrait,
    ApplyBoostCommand.Dispatcher {

    override suspend fun perform(command: ApplyBoostCommand) = command.save()

    private suspend fun ApplyBoostCommand.save(): ApplyBoostCommand.Result {
        val expiration = currentUser.subscriptionDetails()
            .activeExpiration()
            ?: return SubscriptionCommandResult.SubscriptionNotActive

        Boost(currentUser.id, setOf(partyId), expiration)
            .apply { save() }
        return ApplyBoostCommand.Result.Success
    }
}
