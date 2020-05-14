package com.zegreatrob.coupling.action

interface DispatchSyntax {
    val masterDispatcher: MasterDispatcher get() = MasterDispatcher

    fun <D, R> D.execute(action: SuccessfulExecutableAction<D, R>) = masterDispatcher.dispatcho(action, this)

    suspend fun <D, R> D.execute(action: SuspendAction<D, R>) = masterDispatcher.dispatcho(action, this)
}

interface GrandMasterDispatcher : MasterDispatcher, CommandExecuteSyntax, ActionLoggingSyntax {
    override fun <C : SuccessfulExecutableAction<D, R>, D, R> dispatcho(command: C, dispatcher: D): R =
        dispatcher.execute(command)

    override suspend fun <C : SuspendAction<D, R>, D, R> dispatcho(command: C, dispatcher: D) =
        dispatcher.execute(command)
}
