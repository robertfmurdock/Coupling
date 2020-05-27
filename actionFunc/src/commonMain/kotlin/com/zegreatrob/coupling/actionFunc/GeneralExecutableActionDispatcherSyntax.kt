package com.zegreatrob.coupling.actionFunc

interface GeneralExecutableActionDispatcherSyntax : ExecutableActionExecuteSyntax {
    val generalDispatcher: GeneralExecutableActionDispatcher
    override fun <D, R> D.execute(action: ExecutableAction<D, R>) = generalDispatcher.dispatch(action, this)
}
