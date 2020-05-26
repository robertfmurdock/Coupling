package com.zegreatrob.coupling.actionFunc

interface MasterDispatchSyntax : MasterExecutableActionExecuteSyntax, MasterSuspendActionExecuteSyntax {
    override val masterDispatcher: MasterDispatcher
}

interface MasterSuspendActionExecuteSyntax : SuspendActionExecuteSyntax {
    val masterDispatcher: SuspendActionDispatcher
    override suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = masterDispatcher.dispatch(action, this)
}

interface MasterExecutableActionExecuteSyntax : ExecutableActionExecuteSyntax {
    val masterDispatcher: ExecutableActionDispatcher
    override fun <D, R> D.execute(action: ExecutableAction<D, R>) = masterDispatcher.dispatch(action, this)
}
