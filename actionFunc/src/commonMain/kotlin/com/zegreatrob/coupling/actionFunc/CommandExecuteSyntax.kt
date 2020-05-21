package com.zegreatrob.coupling.actionFunc

interface CommandExecuteSyntax {

    fun <C : ExecutableAction<D, R>, D, R> D.execute(command: C): R

    fun <C : ExecutableResultAction<D, R>, D, R> D.execute(command: C): Result<R>

    suspend fun <C : SuspendResultAction<D, R>, D, R> D.execute(command: C): Result<R>

}

suspend fun <D : CommandExecuteSyntax, Q : SuspendResultAction<D, R>, R> D.execute(command: Q) = execute(command)
