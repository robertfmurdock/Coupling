package com.zegreatrob.coupling.action

interface CommandExecutor<out D> {
    fun <C : ExecutableAction<D, R>, R> execute(command: C): Result<R>
    fun <C : SuccessfulExecutableAction<D, R>, R> execute(command: C): R
    suspend fun <C : SuspendAction<D, R>, R> execute(command: C): Result<R>
}

interface AwesomeCommandExecutor<out D : ActionLoggingSyntax> : CommandExecutor<D>, CommandExecuteSyntax {

    val actionDispatcher: D

    override fun <C : ExecutableAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command)
    override fun <C : SuccessfulExecutableAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command).value
    override suspend fun <C : SuspendAction<D, R>, R> execute(command: C) = actionDispatcher.execute(command)

}
