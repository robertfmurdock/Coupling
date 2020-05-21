package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.CommandExecuteSyntax
import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.SuspendAction

interface LoggingCommandExecuteSyntax : CommandExecuteSyntax, ActionLoggingSyntax {

    override fun <C : ExecutableAction<D, R>, D, R> D.execute(command: C): R = log(command) { command.execute(this) }

    override suspend fun <C : SuspendAction<D, R>, D, R> D.execute(command: C) =
        logAsync(command) { handledExecute(command) }

    private suspend fun <C : SuspendAction<D, R>, D, R> D.handledExecute(command: C) = handleException(command) {
        command.execute(this)
    }

    private inline fun <C : SuspendAction<D, R>, D, R> handleException(command: C, doIt: () -> R) = try {
        doIt()
    } catch (bad: Throwable) {
        logger.error(bad) { "Error executing ${command::class.simpleName}" }
        throw bad
    }

}
