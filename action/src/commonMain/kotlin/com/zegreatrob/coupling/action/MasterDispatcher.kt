package com.zegreatrob.coupling.action

interface MasterDispatcher {
    operator fun <C : SuccessfulExecutableAction<D, R>, D, R> invoke(command: C, dispatcher: D): R

    suspend operator fun <C : SuspendAction<D, R>, D, R> invoke(command: C, dispatcher: D): Result<R>

    companion object : MasterDispatcher {
        override fun <C : SuccessfulExecutableAction<D, R>, D, R> invoke(command: C, dispatcher: D) =
            command.execute(dispatcher).value

        override suspend fun <C : SuspendAction<D, R>, D, R> invoke(command: C, dispatcher: D): Result<R> =
            command.execute(dispatcher)

    }
}
