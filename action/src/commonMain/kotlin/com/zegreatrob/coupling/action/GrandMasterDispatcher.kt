package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.DispatchSyntax
import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.MasterDispatcher
import com.zegreatrob.coupling.actionFunc.SuspendResultAction

interface GrandMasterDispatcher : MasterDispatcher, LoggingCommandExecuteSyntax {

    override fun <C : ExecutableAction<D, R>, D, R> dispatch(command: C, dispatcher: D): R = dispatcher.execute(command)

    override suspend fun <C : SuspendResultAction<D, R>, D, R> dispatch(command: C, dispatcher: D) =
        dispatcher.execute(command)

}

interface GrandMasterDispatchSyntax : GrandMasterDispatcher, DispatchSyntax {
    override val masterDispatcher get() = this
}
