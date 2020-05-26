package com.zegreatrob.coupling.actionFunc

interface SuspendActionDispatcher {
    suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D): R
}
