package com.zegreatrob.coupling.actionFunc

interface ActionExecuteSyntax {
    fun <D, R> D.execute(action: ExecutableAction<D, R>): R
    suspend fun <D, R> D.execute(action: SuspendAction<D, R>): R
}

suspend fun <D : ActionExecuteSyntax, A : SuspendAction<D, R>, R> D.execute(action: A) = execute(action)
