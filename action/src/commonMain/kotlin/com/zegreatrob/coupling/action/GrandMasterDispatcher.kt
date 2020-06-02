package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.ExecutableAction
import com.zegreatrob.testmints.action.GeneralExecutableActionDispatcher
import com.zegreatrob.testmints.action.GeneralExecutableActionDispatcherSyntax
import com.zegreatrob.testmints.action.async.GeneralSuspendActionDispatcher
import com.zegreatrob.testmints.action.async.GeneralSuspendActionDispatcherSyntax
import com.zegreatrob.testmints.action.async.SuspendAction

interface GrandMasterDispatcher : GeneralExecutableActionDispatcher, GeneralSuspendActionDispatcher,
    LoggingActionExecuteSyntax {

    override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R = dispatcher.execute(action)

    override suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D) = dispatcher.execute(action)

}

interface GrandMasterDispatchSyntax : GeneralExecutableActionDispatcherSyntax,
    GeneralSuspendActionDispatcherSyntax, TraceIdSyntax {
    override val generalDispatcher: GrandMasterDispatcher
        get() = object : GrandMasterDispatcher, TraceIdSyntax by this {
        }
}
