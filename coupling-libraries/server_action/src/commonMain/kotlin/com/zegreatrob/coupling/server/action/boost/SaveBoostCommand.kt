package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.repository.BoostSave

interface ServerSaveBoostCommandDispatcher : BoostSaveSyntax, AuthenticatedUserSyntax, SaveBoostCommand.Dispatcher {

    override suspend fun perform(command: SaveBoostCommand) = command.save().successResult()

    private suspend fun SaveBoostCommand.save() = Boost(user.id, partyIds).apply { save() }
}

interface BoostSaveSyntax {

    val boostRepository: BoostSave

    suspend fun Boost.save() {
        boostRepository.save(this)
    }
}
