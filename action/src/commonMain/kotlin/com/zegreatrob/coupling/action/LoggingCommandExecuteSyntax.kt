package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.*

interface LoggingCommandExecuteSyntax : CommandExecuteSyntax, ActionLoggingSyntax {

    override fun <C : ExecutableAction<D, R>, D, R> D.execute(command: C) =
        log(command) { command.execute(this) }

    override fun <C : SuccessfulExecutableAction<D, R>, D, R> D.execute(command: C) =
        log(command) { command.execute(this) }.value

    override suspend fun <C : SuspendAction<D, R>, D, R> D.execute(command: C) =
        logAsync(command) { handledExecute(command) }

    private suspend fun <C : SuspendAction<D, R>, D, R> D.handledExecute(command: C) = handleException(command) {
        command.execute(this)
    }

    private inline fun <C : SuspendAction<D, R>, D, R> handleException(command: C, doIt: () -> Result<R>) = try {
        doIt()
    } catch (bad: Throwable) {
        logger.error(bad) { "Error executing ${command::class.simpleName}" }
        ErrorResult<R>(bad.message ?: "")
    }

}
