package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.*
import com.zegreatrob.coupling.actionFunc.async.SuspendAction
import com.zegreatrob.coupling.actionFunc.async.SuspendActionDispatcher
import com.zegreatrob.coupling.actionFunc.async.SuspendActionDispatcherSyntax

interface GrandMasterDispatcher : ExecutableActionDispatcher,
    SuspendActionDispatcher, LoggingActionExecuteSyntax {

    override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R =
        dispatcher.execute(action)

    override suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D) =
        dispatcher.execute(action)

}

interface GrandMasterDispatchSyntax : ExecutableActionDispatcherSyntax,
    SuspendActionDispatcherSyntax,
    TraceIdSyntax {
    override val dispatcher: GrandMasterDispatcher
        get() = object : GrandMasterDispatcher, TraceIdSyntax by this {
        }
}
