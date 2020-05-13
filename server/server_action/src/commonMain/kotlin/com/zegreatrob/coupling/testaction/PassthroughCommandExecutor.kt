package com.zegreatrob.coupling.testaction

import com.zegreatrob.coupling.action.ExecutableAction
import com.zegreatrob.coupling.action.SuccessfulExecutableAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CommandExecutor

interface PassthroughCommandExecutor<out D> : CommandExecutor<D> {
    val actionDispatcher: D
    override fun <C : ExecutableAction<D, R>, R> execute(command: C) = command.execute(actionDispatcher)

    override fun <C : SuccessfulExecutableAction<D, R>, R> execute(command: C) = command.execute(actionDispatcher)
}