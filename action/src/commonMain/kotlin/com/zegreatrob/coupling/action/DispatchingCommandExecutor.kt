package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.*

interface DispatchingCommandExecutor<out D> : ResultCommandExecutor<D>, CommandExecuteSyntax {

    val actionDispatcher: D

    override fun <C : ExecutableAction<D, R>, R> invoke(command: C): R = actionDispatcher.execute(command)
    override suspend fun <C : SuspendAction<D, R>, R> invoke(command: C): R = TODO("Not yet implemented")

    override fun <C : ExecutableResultAction<D, R>, R> invoke(command: C) = actionDispatcher.execute(command)
    override suspend fun <C : SuspendResultAction<D, R>, R> invoke(command: C) = actionDispatcher.execute(command)

}
