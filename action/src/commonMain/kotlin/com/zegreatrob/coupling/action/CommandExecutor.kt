package com.zegreatrob.coupling.action

interface CommandExecutor<out D> {
    fun <C : ExecutableAction<D, R>, R> execute(command: C): Result<R>
    fun <C : SuccessfulExecutableAction<D, R>, R> execute(command: C): R
    suspend fun <C : SuspendAction<D, R>, R> execute(command: C): Result<R>
}

interface DispatchingCommandExecutor<out D> : CommandExecutor<D>, CommandExecuteSyntax {

    val actionDispatcher: D

    override fun <C : ExecutableAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command)
    override fun <C : SuccessfulExecutableAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command)
    override suspend fun <C : SuspendAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command)

}
