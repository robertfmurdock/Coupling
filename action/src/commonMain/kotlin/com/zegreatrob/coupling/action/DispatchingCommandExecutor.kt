package com.zegreatrob.coupling.action

interface DispatchingCommandExecutor<out D> : CommandExecutor<D>, CommandExecuteSyntax {

    val actionDispatcher: D

    override fun <C : ExecutableAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command)
    override fun <C : SuccessfulExecutableAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command)
    override suspend fun <C : SuspendAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command)

}
