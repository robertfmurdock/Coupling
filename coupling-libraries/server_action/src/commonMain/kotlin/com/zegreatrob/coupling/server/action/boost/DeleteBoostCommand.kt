package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.DeleteBoostCommand
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.repository.BoostDelete

interface ServerDeleteBoostCommandDispatcher : BoostDeleteSyntax, DeleteBoostCommand.Dispatcher {

    override suspend fun perform(command: DeleteBoostCommand): SuccessfulResult<Unit> {
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
