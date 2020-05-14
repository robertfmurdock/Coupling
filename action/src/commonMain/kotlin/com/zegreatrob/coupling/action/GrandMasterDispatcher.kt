package com.zegreatrob.coupling.action

interface GrandMasterDispatcher : MasterDispatcher, LoggingCommandExecuteSyntax {
    override fun <C : SuccessfulExecutableAction<D, R>, D, R> dispatcho(command: C, dispatcher: D): R =
        dispatcher.execute(command)

    override suspend fun <C : SuspendAction<D, R>, D, R> dispatcho(command: C, dispatcher: D) =
        dispatcher.execute(command)
}
