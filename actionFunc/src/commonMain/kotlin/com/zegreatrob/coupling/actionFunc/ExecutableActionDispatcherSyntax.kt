package com.zegreatrob.coupling.actionFunc

interface ExecutableActionDispatcherSyntax : ExecutableActionExecuteSyntax {
    val dispatcher: ExecutableActionDispatcher
    override fun <D, R> D.execute(action: ExecutableAction<D, R>) = dispatcher.dispatch(action, this)
}
