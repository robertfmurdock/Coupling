package com.zegreatrob.coupling.action

interface DispatchSyntax {
    val masterDispatcher: MasterDispatcher get() = MasterDispatcher

    fun <D, R> D.execute(action: SuccessfulExecutableAction<D, R>) = masterDispatcher.invoke(action, this)
}
