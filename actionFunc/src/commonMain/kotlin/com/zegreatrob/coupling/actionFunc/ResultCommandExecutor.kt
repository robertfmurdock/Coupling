package com.zegreatrob.coupling.actionFunc

interface CommandExecutor<out D> {
    operator fun <C : ExecutableAction<D, R>, R> invoke(command: C): R
    suspend operator fun <C : SuspendAction<D, R>, R> invoke(command: C): R
}
