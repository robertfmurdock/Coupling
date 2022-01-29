package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.repository.BoostSave

data class SaveBoostCommand(val id: String, val tribeIds: Set<TribeId>) :
    SimpleSuspendResultAction<SaveBoostCommandDispatcher, Boost> {
    override val performFunc = link(SaveBoostCommandDispatcher::perform)
}

interface SaveBoostCommandDispatcher : BoostSaveSyntax, AuthenticatedUserSyntax {

    suspend fun perform(command: SaveBoostCommand) = command.save().successResult()

    private suspend fun SaveBoostCommand.save() = Boost(id, user.id, tribeIds).apply { save() }

}

interface BoostSaveSyntax {

    val boostRepository: BoostSave

    suspend fun Boost.save() {
        boostRepository.save(this)
    }
}
