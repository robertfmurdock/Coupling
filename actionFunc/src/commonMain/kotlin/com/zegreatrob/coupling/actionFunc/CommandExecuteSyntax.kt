package com.zegreatrob.coupling.actionFunc

interface CommandExecuteSyntax {
    fun <D, R> D.execute(action: ExecutableAction<D, R>): R
    suspend fun <D, R> D.execute(action: SuspendAction<D, R>): R
}

suspend fun <D : CommandExecuteSyntax, Q : SuspendAction<D, R>, R> D.execute(command: Q) = execute(command)
