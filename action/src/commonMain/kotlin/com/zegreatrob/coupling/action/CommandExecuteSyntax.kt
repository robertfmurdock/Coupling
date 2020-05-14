package com.zegreatrob.coupling.action

interface CommandExecuteSyntax {

    fun <C : ExecutableAction<D, R>, D, R> D.execute(command: C): Result<R>

    fun <C : SuccessfulExecutableAction<D, R>, D, R> D.execute(command: C): R

    suspend fun <C : SuspendAction<D, R>, D, R> D.execute(command: C): Result<R>

}

suspend fun <D : CommandExecuteSyntax, Q : SuspendAction<D, R>, R> D.execute(command: Q) = execute(command)
