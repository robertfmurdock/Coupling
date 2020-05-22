package com.zegreatrob.coupling.actionFunc

interface CommandExecutor<out D> {
    operator fun <R> invoke(action: ExecutableAction<D, R>): R
    suspend operator fun <R> invoke(action: SuspendAction<D, R>): R
}
