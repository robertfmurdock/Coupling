package com.zegreatrob.coupling.actionFunc

interface MasterDispatchSyntax : CommandExecuteSyntax {
    val masterDispatcher: MasterDispatcher

    override fun <D, R> D.execute(action: ExecutableAction<D, R>) = masterDispatcher.dispatch(action, this)

    override suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = masterDispatcher.dispatch(action, this)
}
