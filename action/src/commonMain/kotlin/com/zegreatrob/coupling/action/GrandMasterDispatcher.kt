package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.GeneralExecutableActionDispatcher
import com.zegreatrob.coupling.actionFunc.GeneralExecutableActionDispatcherSyntax
import com.zegreatrob.coupling.actionFunc.async.GeneralSuspendActionDispatcher
import com.zegreatrob.coupling.actionFunc.async.GeneralSuspendActionDispatcherSyntax
import com.zegreatrob.coupling.actionFunc.async.SuspendAction

interface GrandMasterDispatcher : GeneralExecutableActionDispatcher,
    GeneralSuspendActionDispatcher, LoggingActionExecuteSyntax {

    override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R =
        dispatcher.execute(action)

    override suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D) =
        dispatcher.execute(action)

}

interface GrandMasterDispatchSyntax : GeneralExecutableActionDispatcherSyntax,
    GeneralSuspendActionDispatcherSyntax,
    TraceIdSyntax {
    override val generalDispatcher: GrandMasterDispatcher
        get() = object : GrandMasterDispatcher, TraceIdSyntax by this {
        }
}
