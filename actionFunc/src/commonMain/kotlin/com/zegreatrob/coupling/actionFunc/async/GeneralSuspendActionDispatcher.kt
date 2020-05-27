package com.zegreatrob.coupling.actionFunc.async

interface GeneralSuspendActionDispatcher {
    suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D): R
}
