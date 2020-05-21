package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.DispatchSyntax
import com.zegreatrob.coupling.actionFunc.MasterDispatcher
import com.zegreatrob.coupling.actionFunc.SuccessfulExecutableAction
import com.zegreatrob.coupling.actionFunc.SuspendAction

interface GrandMasterDispatcher : MasterDispatcher, LoggingCommandExecuteSyntax {

    override fun <C : SuccessfulExecutableAction<D, R>, D, R> dispatch(command: C, dispatcher: D): R =
        dispatcher.execute(command)

    override suspend fun <C : SuspendAction<D, R>, D, R> dispatch(command: C, dispatcher: D) =
        dispatcher.execute(command)

}

interface GrandMasterDispatchSyntax: GrandMasterDispatcher,
    DispatchSyntax {
    override val masterDispatcher get() = this
}
