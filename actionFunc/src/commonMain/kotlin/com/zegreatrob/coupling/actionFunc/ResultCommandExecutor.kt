package com.zegreatrob.coupling.actionFunc

interface CommandExecutor<out D> {
    operator fun <C : ExecutableAction<D, R>, R> invoke(command: C): R
    suspend operator fun <C : SuspendAction<D, R>, R> invoke(command: C): R
}

interface ResultCommandExecutor<out D> : CommandExecutor<D> {
    operator fun <C : ExecutableResultAction<D, R>, R> invoke(command: C): Result<R>
    suspend operator fun <C : SuspendResultAction<D, R>, R> invoke(command: C): Result<R>
}
