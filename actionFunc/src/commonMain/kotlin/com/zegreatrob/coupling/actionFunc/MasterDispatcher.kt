package com.zegreatrob.coupling.actionFunc

interface MasterDispatcher {
    fun <C : ExecutableAction<D, R>, D, R> dispatch(command: C, dispatcher: D): R

    suspend fun <C : SuspendAction<D, R>, D, R> dispatch(command: C, dispatcher: D): R

    companion object : MasterDispatcher {
        override fun <C : ExecutableAction<D, R>, D, R> dispatch(command: C, dispatcher: D): R =
            command.execute(dispatcher)

        override suspend fun <C : SuspendAction<D, R>, D, R> dispatch(command: C, dispatcher: D) =
            command.execute(dispatcher)
    }
}
