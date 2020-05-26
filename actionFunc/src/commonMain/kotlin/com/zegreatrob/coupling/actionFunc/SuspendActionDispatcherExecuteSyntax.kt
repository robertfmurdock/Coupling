package com.zegreatrob.coupling.actionFunc

interface SuspendActionDispatcherExecuteSyntax : SuspendActionExecuteSyntax {
    val dispatcher: SuspendActionDispatcher
    override suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = dispatcher.dispatch(action, this)
}
