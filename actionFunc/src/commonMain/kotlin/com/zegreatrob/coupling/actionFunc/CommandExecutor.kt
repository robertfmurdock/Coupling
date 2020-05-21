package com.zegreatrob.coupling.actionFunc

interface CommandExecutor<out D> {
    operator fun <C : ExecutableResultAction<D, R>, R> invoke(command: C): Result<R>
    operator fun <C : SuccessfulExecutableAction<D, R>, R> invoke(command: C): R
    suspend operator fun <C : SuspendResultAction<D, R>, R> invoke(command: C): Result<R>
}
