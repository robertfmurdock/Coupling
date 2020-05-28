package com.zegreatrob.coupling.actionFunc

interface GeneralExecutableActionDispatcher {
    fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R

    companion object : GeneralExecutableActionDispatcher, ExecutableActionExecuteSyntax {
        override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R = dispatcher.execute(action)
    }
}
