package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.MasterDispatchSyntax
import com.zegreatrob.coupling.actionFunc.MasterDispatcher
import com.zegreatrob.coupling.actionFunc.SuspendAction

interface GrandMasterDispatcher : MasterDispatcher, LoggingCommandExecuteSyntax {

    override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R =
        dispatcher.execute(action)

    override suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D) =
        dispatcher.execute(action)

}

interface GrandMasterDispatchSyntax : MasterDispatchSyntax, TraceIdSyntax {
    override val masterDispatcher: GrandMasterDispatcher
        get() = object : GrandMasterDispatcher, TraceIdSyntax by this {
        }
}
