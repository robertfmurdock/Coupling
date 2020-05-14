package com.zegreatrob.coupling.action

interface CommandExecutor<out D> {
    operator fun <C : ExecutableAction<D, R>, R> invoke(command: C): Result<R>
    operator fun <C : SuccessfulExecutableAction<D, R>, R> invoke(command: C): R
    suspend operator fun <C : SuspendAction<D, R>, R> invoke(command: C): Result<R>
}
