package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class GrantSlackAccessCommand(val code: String, val state: String) :
    SimpleSuspendAction<GrantSlackAccessCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: GrantSlackAccessCommand): VoidResult
    }
}
