package com.zegreatrob.coupling.actionFunc

interface ExecutableActionExecuteSyntax {
    fun <D, R> D.execute(action: ExecutableAction<D, R>): R = action.execute(this)
}
