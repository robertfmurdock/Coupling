package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax

interface ServerSaveBoostCommandDispatcher : BoostSaveSyntax, AuthenticatedUserSyntax, SaveBoostCommand.Dispatcher {

    override suspend fun perform(command: SaveBoostCommand) = command.save().successResult()

    private suspend fun SaveBoostCommand.save() {
        Boost(user.id, partyIds).apply { save() }
    }
}
