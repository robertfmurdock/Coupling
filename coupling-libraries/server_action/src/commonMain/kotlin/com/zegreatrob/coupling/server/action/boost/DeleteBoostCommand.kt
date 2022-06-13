package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.repository.BoostDelete

class DeleteBoostCommand : SimpleSuspendResultAction<DeleteBoostCommandDispatcher, Unit> {
    override val performFunc = link(DeleteBoostCommandDispatcher::perform)
}

interface DeleteBoostCommandDispatcher : BoostDeleteSyntax {

    suspend fun perform(command: DeleteBoostCommand): SuccessfulResult<Unit> {
        deleteIt()
        return SuccessfulResult(Unit)
    }
}

interface BoostDeleteSyntax {

    val boostRepository: BoostDelete

    suspend fun deleteIt() {
        boostRepository.deleteIt()
    }
}
