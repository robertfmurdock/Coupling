package com.zegreatrob.coupling.actionFunc

interface ExecutableActionExecutor<out D> {
    operator fun <R> invoke(action: ExecutableAction<D, R>): R
}
