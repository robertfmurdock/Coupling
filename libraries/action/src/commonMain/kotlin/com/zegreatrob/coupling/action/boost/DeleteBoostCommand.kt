package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class DeleteBoostCommand : SimpleSuspendAction<DeleteBoostCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeleteBoostCommand): VoidResult
    }
}
