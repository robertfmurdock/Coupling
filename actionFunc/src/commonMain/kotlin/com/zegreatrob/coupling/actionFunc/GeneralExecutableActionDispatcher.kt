package com.zegreatrob.coupling.actionFunc

interface GeneralExecutableActionDispatcher {
    fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R

    companion object : GeneralExecutableActionDispatcher {
        override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R =
            action.execute(dispatcher)
    }
}
