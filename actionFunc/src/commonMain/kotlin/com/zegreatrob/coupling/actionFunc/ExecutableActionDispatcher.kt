package com.zegreatrob.coupling.actionFunc

interface ExecutableActionDispatcher {
    fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R

    companion object : ExecutableActionDispatcher {
        override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R =
            action.execute(dispatcher)
    }
}
