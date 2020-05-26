package com.zegreatrob.coupling.actionFunc.async

interface SuspendActionDispatcherSyntax :
    SuspendActionExecuteSyntax {
    val dispatcher: SuspendActionDispatcher
    override suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = dispatcher.dispatch(action, this)
}
