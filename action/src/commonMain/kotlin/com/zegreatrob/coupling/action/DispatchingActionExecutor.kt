package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.*

interface DispatchingActionExecutor<out D> : ExecutableActionExecutor<D>, SuspendActionExecutor<D>,
    ExecutableActionExecuteSyntax, SuspendActionExecuteSyntax {

    val actionDispatcher: D

    override fun <R> invoke(action: ExecutableAction<D, R>): R = actionDispatcher.execute(action)
    override suspend fun <R> invoke(action: SuspendAction<D, R>): R = actionDispatcher.execute(action)

}
