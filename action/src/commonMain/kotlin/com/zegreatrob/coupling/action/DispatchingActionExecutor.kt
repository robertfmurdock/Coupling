package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.ActionExecuteSyntax
import com.zegreatrob.coupling.actionFunc.ActionExecutor
import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.SuspendAction

interface DispatchingActionExecutor<out D> : ActionExecutor<D>, ActionExecuteSyntax {

    val actionDispatcher: D

    override fun <R> invoke(action: ExecutableAction<D, R>): R = actionDispatcher.execute(action)
    override suspend fun <R> invoke(action: SuspendAction<D, R>): R = actionDispatcher.execute(action)

}
