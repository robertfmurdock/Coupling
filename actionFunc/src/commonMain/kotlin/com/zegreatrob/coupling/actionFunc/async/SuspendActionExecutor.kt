package com.zegreatrob.coupling.actionFunc.async

interface SuspendActionExecutor<out D> {
    suspend operator fun <R> invoke(action: SuspendAction<D, R>): R
}
