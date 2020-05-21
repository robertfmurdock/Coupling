package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.MasterDispatchSyntax
import com.zegreatrob.coupling.actionFunc.MasterDispatcher
import com.zegreatrob.coupling.actionFunc.SuspendAction

interface GrandMasterDispatcher : MasterDispatcher, LoggingCommandExecuteSyntax {

    override fun <C : ExecutableAction<D, R>, D, R> dispatch(command: C, dispatcher: D): R =
        dispatcher.execute(command)

    override suspend fun <C : SuspendAction<D, R>, D, R> dispatch(command: C, dispatcher: D) =
        dispatcher.execute(command)

}

interface GrandMasterDispatchSyntax : MasterDispatchSyntax, TraceIdSyntax {
    override val masterDispatcher: GrandMasterDispatcher
        get() = object : GrandMasterDispatcher, TraceIdSyntax by this {
        }
}
