package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.CommandExecuteSyntax
import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.ResultCommandExecutor
import com.zegreatrob.coupling.actionFunc.SuspendAction

interface DispatchingCommandExecutor<out D> : ResultCommandExecutor<D>, CommandExecuteSyntax {

    val actionDispatcher: D

    override fun <C : ExecutableAction<D, R>, R> invoke(command: C): R = actionDispatcher.execute(command)
    override suspend fun <C : SuspendAction<D, R>, R> invoke(command: C): R = actionDispatcher.execute(command)

}
