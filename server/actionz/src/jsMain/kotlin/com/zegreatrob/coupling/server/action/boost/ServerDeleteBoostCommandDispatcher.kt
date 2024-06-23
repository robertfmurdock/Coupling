package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand

interface ServerDeleteBoostCommandDispatcher :
    BoostDeleteSyntax,
    DeleteBoostCommand.Dispatcher {

    override suspend fun perform(command: DeleteBoostCommand) = VoidResult.Accepted.also { deleteIt() }
}
