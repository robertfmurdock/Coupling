package com.zegreatrob.coupling.actionFunc

interface DispatchSyntax {
    val masterDispatcher: MasterDispatcher

    fun <D, R> D.execute(action: ExecutableAction<D, R>) = masterDispatcher.dispatch(action, this)

    suspend fun <D, R> D.execute(action: SuspendResultAction<D, R>) = masterDispatcher.dispatch(action, this)
}

