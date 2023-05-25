package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.ExecutableAction
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import com.zegreatrob.testmints.action.ExecutableActionExecutor
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.SuspendActionExecutor

interface DispatchingActionExecutor<out D> :
    ExecutableActionExecutor<D>,
    SuspendActionExecutor<D>,
    ExecutableActionExecuteSyntax,
    SuspendActionExecuteSyntax {

    val actionDispatcher: D

    override fun <R> invoke(action: ExecutableAction<D, R>): R = actionDispatcher.execute(action)
    override suspend fun <R> invoke(action: SuspendAction<D, R>): R = actionDispatcher.execute(action)
}
