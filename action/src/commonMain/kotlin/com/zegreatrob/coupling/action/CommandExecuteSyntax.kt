package com.zegreatrob.coupling.action

interface CommandExecuteSyntax {

    suspend fun <C : SuspendAction<D, R>, D : ActionLoggingSyntax, R> D.execute(command: C) =
        command.logAsync { command.execute(this) }

    fun <C : ExecutableAction<D, R>, D : ActionLoggingSyntax, R> D.execute(command: C) =
        command.log { command.execute(this) }

    fun <C : SuccessfulExecutableAction<D, R>, D : ActionLoggingSyntax, R> D.execute(command: C) =
        command.log { command.execute(this) }

}
