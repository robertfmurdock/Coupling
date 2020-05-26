package com.zegreatrob.coupling.actionFunc

interface MasterDispatcher : ExecutableActionDispatcher, SuspendActionDispatcher {
    companion object : MasterDispatcher {
        override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R =
            action.execute(dispatcher)

        override suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D) =
            action.execute(dispatcher)
    }
}

interface SuspendActionDispatcher {
    suspend fun <D, R> dispatch(action: SuspendAction<D, R>, dispatcher: D): R
}

interface ExecutableActionDispatcher {
    fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R
}
