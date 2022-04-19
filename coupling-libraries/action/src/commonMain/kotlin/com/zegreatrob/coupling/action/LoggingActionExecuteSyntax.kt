package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.ExecutableAction
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

interface LoggingActionExecuteSyntax :
    ExecutableActionExecuteSyntax,
    SuspendActionExecuteSyntax,
    ActionLoggingSyntax {

    override fun <D, R> D.execute(action: ExecutableAction<D, R>): R = log(action) { action.execute(this) }

    override suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = logAsync(action) { handledExecute(action) }

    private suspend fun <D, R> D.handledExecute(action: SuspendAction<D, R>) = handleException(action) {
        action.execute(this)
    }

    private inline fun <D, R> handleException(action: SuspendAction<D, R>, doIt: () -> R) = try {
        doIt()
    } catch (bad: Throwable) {
        logger.error(bad) { "Error executing ${action::class.simpleName}" }
        throw bad
    }
}
