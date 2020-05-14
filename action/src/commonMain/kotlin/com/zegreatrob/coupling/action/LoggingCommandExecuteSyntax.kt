package com.zegreatrob.coupling.action

interface LoggingCommandExecuteSyntax : CommandExecuteSyntax, ActionLoggingSyntax {

    override suspend fun <C : SuspendAction<D, R>, D, R> D.execute(command: C) =
        logAsync(command) { command.execute(this) }

    override fun <C : ExecutableAction<D, R>, D, R> D.execute(command: C) =
        log(command) { command.execute(this) }

    override fun <C : SuccessfulExecutableAction<D, R>, D, R> D.execute(command: C) =
        log(command) { command.execute(this) }.value

}

interface CommandExecuteSyntax {

    suspend fun <C : SuspendAction<D, R>, D, R> D.execute(command: C): Result<R>

    fun <C : ExecutableAction<D, R>, D, R> D.execute(command: C): Result<R>

    fun <C : SuccessfulExecutableAction<D, R>, D, R> D.execute(command: C): R

}

suspend fun <D : CommandExecuteSyntax, Q : SuspendAction<D, R>, R> D.execute(command: Q) = execute(command)
