package com.zegreatrob.coupling.action

interface LoggingCommandExecuteSyntax : CommandExecuteSyntax, ActionLoggingSyntax {

    override fun <C : ExecutableAction<D, R>, D, R> D.execute(command: C) =
        log(command) { command.execute(this) }

    override fun <C : SuccessfulExecutableAction<D, R>, D, R> D.execute(command: C) =
        log(command) { command.execute(this) }.value

    override suspend fun <C : SuspendAction<D, R>, D, R> D.execute(command: C) =
        logAsync(command) { command.execute(this) }

}
