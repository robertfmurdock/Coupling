package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.CommandExecuteSyntax
import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.SuspendAction

interface LoggingCommandExecuteSyntax : CommandExecuteSyntax, ActionLoggingSyntax {

    override fun <D, R> D.execute(command: ExecutableAction<D, R>): R = log(command) { command.execute(this) }

    override suspend fun <D, R> D.execute(command: SuspendAction<D, R>) = logAsync(command) { handledExecute(command) }

    private suspend fun <D, R> D.handledExecute(command: SuspendAction<D, R>) = handleException(command) {
        command.execute(this)
    }

    private inline fun <D, R> handleException(command: SuspendAction<D, R>, doIt: () -> R) = try {
        doIt()
    } catch (bad: Throwable) {
        logger.error(bad) { "Error executing ${command::class.simpleName}" }
        throw bad
    }

}
