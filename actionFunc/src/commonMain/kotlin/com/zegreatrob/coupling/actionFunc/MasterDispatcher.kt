package com.zegreatrob.coupling.actionFunc

interface MasterDispatcher {
    fun <C : SuccessfulExecutableAction<D, R>, D, R> dispatch(command: C, dispatcher: D): R

    suspend fun <C : SuspendAction<D, R>, D, R> dispatch(command: C, dispatcher: D): Result<R>

    companion object : MasterDispatcher {
        override fun <C : SuccessfulExecutableAction<D, R>, D, R> dispatch(command: C, dispatcher: D) =
            command.execute(dispatcher).value

        override suspend fun <C : SuspendAction<D, R>, D, R> dispatch(command: C, dispatcher: D) =
            command.execute(dispatcher)
    }
}
