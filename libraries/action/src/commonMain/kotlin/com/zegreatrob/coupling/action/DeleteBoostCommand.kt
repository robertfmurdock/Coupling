package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class DeleteBoostCommand : SimpleSuspendAction<DeleteBoostCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeleteBoostCommand): SuccessfulResult<Unit>
    }
}
