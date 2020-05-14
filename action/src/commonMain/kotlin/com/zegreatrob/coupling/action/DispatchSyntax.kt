package com.zegreatrob.coupling.action

interface DispatchSyntax {
    val masterDispatcher: MasterDispatcher

    fun <D, R> D.execute(action: SuccessfulExecutableAction<D, R>) = masterDispatcher.dispatch(action, this)

    suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = masterDispatcher.dispatch(action, this)
}

