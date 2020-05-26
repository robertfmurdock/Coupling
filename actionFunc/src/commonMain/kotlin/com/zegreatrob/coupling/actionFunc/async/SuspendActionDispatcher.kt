package com.zegreatrob.coupling.actionFunc.async

interface SuspendActionDispatcher {
    suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D): R
}
