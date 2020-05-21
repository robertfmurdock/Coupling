package com.zegreatrob.coupling.actionFunc

interface CommandExecuteSyntax {

    fun <C : ExecutableResultAction<D, R>, D, R> D.execute(command: C): Result<R>

    fun <C : SuccessfulExecutableAction<D, R>, D, R> D.execute(command: C): R

    suspend fun <C : SuspendResultAction<D, R>, D, R> D.execute(command: C): Result<R>

}

suspend fun <D : CommandExecuteSyntax, Q : SuspendResultAction<D, R>, R> D.execute(command: Q) = execute(command)
